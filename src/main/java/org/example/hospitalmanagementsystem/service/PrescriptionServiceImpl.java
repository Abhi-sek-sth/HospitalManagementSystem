package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.MedicalRecords;
import org.example.hospitalmanagementsystem.entity.Prescription;
import org.example.hospitalmanagementsystem.exception.AppointmentNotCompletedException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.PrescriptionMapper;
import org.example.hospitalmanagementsystem.repository.MedicalRecordsRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionRepository;
import org.example.hospitalmanagementsystem.util.PrescriptionAccessPolicy;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Sort DEFAULT_SORT = Sort.by("prescribedAt").descending();

    private final MedicalRecordsRepository medicalRecordsRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionAccessPolicy accessPolicy;
    private final SecurityUtil securityUtil;
    private final Clock clock;

    @Transactional
    @Override
    public PrescriptionResponseDto createPrescription(PrescriptionRequestDto request) {
        accessPolicy.requireDoctorRole("create");
        Long userId = securityUtil.getCurrentUserId();

        MedicalRecords medicalRecord = medicalRecordsRepository
                .findByIdWithAppointmentAndDoctor(request.medicalRecordId())
                .orElseThrow(() -> medicalRecordNotFound(request.medicalRecordId()));

        Appointment appointment = appointmentOf(medicalRecord);

        accessPolicy.requireOwningDoctor(appointment, userId,
                () -> "You are not allowed to prescribe medication for this medical record");
        accessPolicy.requireCompleted(appointment);
        accessPolicy.requireActiveDoctor(appointment.getDoctor(),
                "Doctor is inactive and cannot create prescriptions");

        Prescription prescription = prescriptionMapper.toEntity(request);
        prescription.setMedicalRecord(medicalRecord);
        Prescription saved = prescriptionRepository.save(prescription);

        log.info("Prescription {} created for medical record {} by user {}",
                saved.getId(), medicalRecord.getId(), userId);

        return prescriptionMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PrescriptionResponseDto getPrescriptionById(Long prescriptionId) {
        Prescription prescription = findPrescriptionOrThrow(prescriptionId);

        accessPolicy.requireParticipant(
                prescription.getMedicalRecord().getAppointment(),
                securityUtil.getCurrentUserId()
                );

        return prescriptionMapper.toResponseDto(prescription);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponseDto<PrescriptionResponseDto> getPrescriptionsByMedicalRecordId(
            Long medicalRecordId, int page, int size) {

        MedicalRecords medicalRecord = medicalRecordsRepository.findByIdWithAppointmentAndDoctor(medicalRecordId)
                .orElseThrow(() -> medicalRecordNotFound(medicalRecordId));

        accessPolicy.requireParticipant(
                medicalRecord.getAppointment(),
                securityUtil.getCurrentUserId()
                );

        Page<Prescription> result = prescriptionRepository
                .findAllByMedicalRecordId(medicalRecordId, toPageable(page, size));

        return toPageResponse(result);
    }

    @Transactional
    @Override
    public PrescriptionResponseDto updatePrescription(Long prescriptionId, PrescriptionUpdateRequestDto request) {
        accessPolicy.requireDoctorRole("modify");
        Long userId = securityUtil.getCurrentUserId();

        Prescription prescription = findPrescriptionOrThrow(prescriptionId);
        Appointment appointment = appointmentOf(prescription.getMedicalRecord());

        accessPolicy.requireOwningDoctor(appointment, userId, () -> notOwnerMessage("update",prescriptionId));
        accessPolicy.requireCompleted(appointment);
        accessPolicy.requireActiveDoctor(appointment.getDoctor(),
                "Doctor is inactive and cannot modify prescriptions");

        prescriptionMapper.updateEntity(request, prescription);

        log.info("Prescription {} updated by user {}", prescriptionId, userId);

        return prescriptionMapper.toResponseDto(prescription);
    }

    @Transactional
    @Override
    public void deletePrescription(Long prescriptionId) {
        accessPolicy.requireDoctorRole("delete");
        Long userId = securityUtil.getCurrentUserId();

        Prescription prescription = findPrescriptionOrThrow(prescriptionId);
        Appointment appointment = prescription.getMedicalRecord().getAppointment();

        accessPolicy.requireOwningDoctor(appointment, userId, () -> notOwnerMessage("delete",prescriptionId));

        prescription.setDeleted(true);
        prescription.setDeletedAt(LocalDateTime.now(clock));
        prescription.setDeletedBy(userId);
        log.info("Prescription {} soft deleted by user {}", prescriptionId, userId);
    }


    private Prescription findPrescriptionOrThrow(Long prescriptionId) {
        return prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + prescriptionId));
    }

    private static ResourceNotFoundException medicalRecordNotFound(Long id) {
        return new ResourceNotFoundException("Medical record not found with id: " + id);
    }

    private static Appointment appointmentOf(MedicalRecords record) {
        Appointment appointment = record.getAppointment();
        if (appointment == null) {
            throw new AppointmentNotCompletedException("Your appointment has not been completed yet");
        }
        return appointment;
    }

    private static String notOwnerMessage(String action,Long prescriptionId) {
        return "You are not allowed to " + action + " this prescription with id " + prescriptionId;
    }

    private static Pageable toPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, DEFAULT_SORT);
    }

    private PageResponseDto<PrescriptionResponseDto> toPageResponse(Page<Prescription> page) {
        return new PageResponseDto<>(
                page.getContent().stream().map(prescriptionMapper::toResponseDto).toList(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements());
    }
}