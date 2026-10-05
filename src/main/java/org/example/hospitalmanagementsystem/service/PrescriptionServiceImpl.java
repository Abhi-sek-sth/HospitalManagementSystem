package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionResponseDto;
import org.example.hospitalmanagementsystem.entity.*;
import org.example.hospitalmanagementsystem.exception.AppointmentNotCompletedException;
import org.example.hospitalmanagementsystem.exception.ForbiddenOperationException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.PrescriptionMapper;
import org.example.hospitalmanagementsystem.repository.MedicalRecordsRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionRepository;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService{
    private final MedicalRecordsRepository medicalRecordsRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionRepository prescriptionRepository;
    private final SecurityUtil securityUtil;
    private static final int MAX_PAGE_SIZE = 100;


    @Transactional
    @Override
    public PrescriptionResponseDto createPrescription(PrescriptionRequestDto request) {

        validatePrescribingRole();

        MedicalRecords medicalRecord = medicalRecordsRepository
                .findByIdWithAppointmentAndDoctor(request.medicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record not found with id: " + request.medicalRecordId()));


        validatePrescribingPermission(medicalRecord);

        validateMedicalRecordIsCompleted(medicalRecord);
        validateDoctorIsActive(medicalRecord);

        Prescription prescription = prescriptionMapper.toEntity(request);
        prescription.setMedicalRecord(medicalRecord);

        Prescription saved = prescriptionRepository.save(prescription);

        log.info("Prescription {} created for medical record {} by user {}",
                saved.getId(), medicalRecord.getId(), securityUtil.getCurrentUserId());

        return prescriptionMapper.toResponseDto(saved);
    }

    private void validatePrescribingRole() {
        if (!securityUtil.isDoctor()) {
            throw new ForbiddenOperationException("Only doctors can create prescriptions");
        }
    }

    private void validatePrescribingPermission(MedicalRecords medicalRecord) {
        Doctor doctor = medicalRecord.getAppointment().getDoctor();
        Long currentUserId = securityUtil.getCurrentUserId();

        if (!currentUserId.equals(doctor.getUser().getId())) {
            throw new ForbiddenOperationException(
                    "You are not allowed to prescribe medication for this medical record");
        }
    }

    private void validateMedicalRecordIsCompleted(MedicalRecords medicalRecord) {
        Appointment appointment = medicalRecord.getAppointment();
        if (appointment == null || !AppointmentStatus.COMPLETED.equals(appointment.getStatus())) {
            throw new AppointmentNotCompletedException("Your appointment has not been completed yet");
        }
    }

    private void validateDoctorIsActive(MedicalRecords medicalRecord) {
        if (!Boolean.TRUE.equals(medicalRecord.getAppointment().getDoctor().getActive())) {
            throw new ForbiddenOperationException(
                    "Doctor is inactive and cannot create prescriptions");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PrescriptionResponseDto getPrescriptionById(Long prescriptionId) {


        Prescription prescription = findPrescriptionOrThrow(prescriptionId);

        validateCanView(prescription.getMedicalRecord().getAppointment(), prescriptionId);

        return prescriptionMapper.toResponseDto(prescription);
    }
    @Transactional(readOnly = true)
    @Override
    public PageResponseDto<PrescriptionResponseDto> getPrescriptionsByMedicalRecordId(Long medicalRecordId,int page,int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
        MedicalRecords medicalRecord = medicalRecordsRepository.findByIdWithAppointmentAndDoctor(medicalRecordId)
                .orElseThrow(() -> medicalRecordNotFound(medicalRecordId));

        validateCanView(medicalRecord.getAppointment(), medicalRecordId);
        Page<Prescription> prescriptionPage = prescriptionRepository.findAllByMedicalRecordId(medicalRecordId, pageable);

        return new PageResponseDto<>(
                prescriptionPage.getContent()
                        .stream()
                        .map(prescriptionMapper::toResponseDto)
                        .toList(),
                prescriptionPage.getNumber(),
                prescriptionPage.getTotalPages(),
                prescriptionPage.getTotalElements()
        );
    }

    @Transactional
    @Override
    public PrescriptionResponseDto updatePrescription(Long prescriptionId, PrescriptionUpdateRequestDto request) {
        validatePrescribingRoleForUpdate();

        Prescription prescription = findPrescriptionOrThrow(prescriptionId);
        validateMedicalRecordIsCompleted(prescription.getMedicalRecord());
        Appointment appointment = prescription.getMedicalRecord().getAppointment();

        validateIsOwningDoctor(appointment, prescriptionId);
        validateDoctorIsActive(appointment.getDoctor());

        prescriptionMapper.updateEntity(request, prescription);

        log.info("Prescription {} updated by user {}", prescriptionId, securityUtil.getCurrentUserId());

        return prescriptionMapper.toResponseDto(prescription);
    }
    @Transactional
    @Override
    public void deletePrescription(Long prescriptionId) {
        validatePrescribingRoleForDelete();

        Prescription prescription = findPrescriptionOrThrow(prescriptionId);
        Appointment appointment = prescription.getMedicalRecord().getAppointment();

        validateIsOwningDoctor(appointment, prescriptionId);
        validateDoctorIsActive(appointment.getDoctor());

        prescriptionRepository.delete(prescription);

        log.info("Prescription {} deleted by user {}", prescriptionId, securityUtil.getCurrentUserId());

    }
    private Prescription findPrescriptionOrThrow(Long prescriptionId) {
        return prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + prescriptionId));
    }

    private ResourceNotFoundException medicalRecordNotFound(Long id) {
        return new ResourceNotFoundException("Medical record not found with id: " + id);
    }

    private void validateCanView(Appointment appointment, Long resourceId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        boolean isOwningDoctor = currentUserId.equals(appointment.getDoctor().getUser().getId());
        boolean isOwningPatient = currentUserId.equals(appointment.getPatient().getUser().getId());

        if (!isOwningDoctor && !isOwningPatient) {
            throw new ResourceNotFoundException("Resource not found with id: " + resourceId);
        }
    }

    private void validateIsOwningDoctor(Appointment appointment, Long prescriptionId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        if (!currentUserId.equals(appointment.getDoctor().getUser().getId())) {
            throw new ForbiddenOperationException("You are not allowed to update this prescription with id " + prescriptionId);
        }
    }

    private void validateDoctorIsActive(Doctor doctor) {
        if (!Boolean.TRUE.equals(doctor.getActive())) {
            throw new ForbiddenOperationException("Doctor is inactive and cannot modify prescriptions");
        }
    }

    private void validatePrescribingRoleForUpdate() {
        if (!securityUtil.isDoctor()) {
            throw new ForbiddenOperationException("Only doctors can modify prescriptions");
        }
    }
    private void validatePrescribingRoleForDelete() {
        if (!securityUtil.isDoctor()) {
            throw new ForbiddenOperationException("Only doctors with related details can delete prescriptions");
        }
    }

}
