package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionItemResponseDto;
import org.example.hospitalmanagementsystem.entity.*;
import org.example.hospitalmanagementsystem.exception.*;
import org.example.hospitalmanagementsystem.mapper.PrescriptionItemMapper;
import org.example.hospitalmanagementsystem.repository.MedicationRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionItemRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionRepository;
import org.example.hospitalmanagementsystem.util.PrescriptionAccessPolicy;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionItemServiceImpl implements PrescriptionItemService {

    private final PrescriptionItemRepository itemRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionItemMapper itemMapper;
    private final PrescriptionAccessPolicy accessPolicy;
    private final SecurityUtil securityUtil;
    private final Clock clock;

    @Transactional
    @Override
    public PrescriptionItemResponseDto addItem(Long prescriptionId, PrescriptionItemRequestDto request) {
        accessPolicy.requireDoctorRole("modify");
        Long userId = securityUtil.getCurrentUserId();

        Prescription prescription = findPrescriptionOrThrow(prescriptionId);
        requireCanModify(prescription, userId);   // authorize BEFORE touching medication data

        Medication medication = medicationRepository.findById(request.medicationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medication not found with id: " + request.medicationId()));

        if (!medication.isActive()) {
            throw new InvalidOperationException(
                    "Medication with id " + medication.getId() + " is inactive and cannot be prescribed");
        }
        if (itemRepository.existsByPrescriptionIdAndMedicationId(prescriptionId, medication.getId())) {
            throw new AlreadyExistException(
                    "Medication with id " + medication.getId() + " is already part of prescription " + prescriptionId);
        }

        PrescriptionItem item = itemMapper.toEntity(request);
        item.setPrescription(prescription);
        item.setMedication(medication);
        PrescriptionItem saved = itemRepository.save(item);

        log.info("Item {} (medication {}) added to prescription {} by user {}",
                saved.getId(), medication.getId(), prescriptionId, userId);

        return itemMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PrescriptionItemResponseDto getItemById(Long itemId) {
        PrescriptionItem item = findItemOrThrow(itemId);

        accessPolicy.requireParticipant(
                item.getPrescription().getMedicalRecord().getAppointment(),
                securityUtil.getCurrentUserId());

        return itemMapper.toResponseDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PrescriptionItemResponseDto> getItemsByPrescriptionId(Long prescriptionId) {
        Prescription prescription = findPrescriptionOrThrow(prescriptionId);

        accessPolicy.requireParticipant(
                prescription.getMedicalRecord().getAppointment(),
                securityUtil.getCurrentUserId());

        return itemRepository.findAllByPrescriptionIdWithMedication(prescriptionId).stream()
                .map(itemMapper::toResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public PrescriptionItemResponseDto updateItem(Long itemId, PrescriptionItemUpdateRequestDto request) {
        accessPolicy.requireDoctorRole("modify");
        Long userId = securityUtil.getCurrentUserId();

        if (!request.hasAnyField()) {
            throw new InvalidOperationException("At least one field must be provided for update");
        }

        PrescriptionItem item = findItemOrThrow(itemId);
        requireCanModify(item.getPrescription(), userId);

        itemMapper.updateEntity(request, item);
        // flush so @Version conflicts surface here and the response carries the fresh updatedAt
        PrescriptionItem saved = itemRepository.saveAndFlush(item);

        log.info("Prescription item {} updated by user {}", itemId, userId);

        return itemMapper.toResponseDto(saved);
    }

    @Transactional
    @Override
    public void deleteItem(Long itemId) {
        accessPolicy.requireDoctorRole("modify");
        Long userId = securityUtil.getCurrentUserId();

        PrescriptionItem item = findItemOrThrow(itemId);
        requireCanModify(item.getPrescription(), userId);

        item.setDeleted(true);
        item.setDeletedAt(LocalDateTime.now(clock));
        item.setDeletedBy(userId);

        log.info("Prescription item {} soft deleted by user {}", itemId, userId);
    }

    // ---------------------------------------------------------------- helpers

    /**
     * Ownership is derived server-side from the persisted chain
     * prescription → medical record → appointment → doctor → user, compared with the
     * authenticated user id from the JWT. No client-supplied id is trusted for authorization.
     */
    private void requireCanModify(Prescription prescription, Long userId) {
        Appointment appointment = appointmentOf(prescription.getMedicalRecord());

        accessPolicy.requireOwningDoctor(appointment, userId,
                () -> "You are not allowed to modify items of prescription with id " + prescription.getId());
        accessPolicy.requireCompleted(appointment);
        accessPolicy.requireActiveDoctor(appointment.getDoctor(),
                "Doctor is inactive and cannot modify prescriptions");
    }

    private Prescription findPrescriptionOrThrow(Long prescriptionId) {
        return prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(prescriptionId)
                .filter(p -> !p.isDeleted())   // defensive: never operate on a soft-deleted prescription
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + prescriptionId));
    }

    private PrescriptionItem findItemOrThrow(Long itemId) {
        return itemRepository.findByIdWithContext(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription item not found with id: " + itemId));
    }

    // Mirrors PrescriptionServiceImpl.appointmentOf (see review: move to the policy)
    private static Appointment appointmentOf(MedicalRecords record) {
        Appointment appointment = record.getAppointment();
        if (appointment == null) {
            throw new AppointmentNotCompletedException("Your appointment has not been completed yet");
        }
        return appointment;
    }
}