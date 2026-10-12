package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionItemResponseDto;
import org.example.hospitalmanagementsystem.entity.*;
import org.example.hospitalmanagementsystem.exception.*;
import org.example.hospitalmanagementsystem.mapper.PrescriptionItemMapper;
import org.example.hospitalmanagementsystem.persistence.ConstraintViolationClassifier;
import org.example.hospitalmanagementsystem.repository.MedicationRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionItemRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionRepository;
import org.example.hospitalmanagementsystem.util.PrescriptionAccessPolicy;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionItemServiceImpl implements PrescriptionItemService {

    private static final String ACTIVE_ITEM_UNIQUE_INDEX = "uq_prescription_items_active_medication";
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
        Long userId = currentDoctorUserId();

        Prescription prescription = findPrescriptionOrThrow(prescriptionId);
        requireCanModify(prescription, userId);

        Medication medication = medicationRepository.findById(request.medicationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medication not found with id: " + request.medicationId()));

        if (!medication.isActive()) {
            throw new InvalidOperationException(
                    "Medication with id " + medication.getId() + " is inactive and cannot be prescribed");
        }
        if (itemRepository.existsByPrescriptionIdAndMedicationId(prescriptionId, medication.getId())) {
            throw duplicateItem(prescriptionId, medication.getId());
        }

        PrescriptionItem item = itemMapper.toEntity(request);
        item.setPrescription(prescription);
        item.setMedication(medication);
        PrescriptionItem saved = saveOrTranslateDuplicate(item, prescriptionId, medication.getId());

        log.info("Item {} (medication {}) added to prescription {} by user {}",
                saved.getId(), medication.getId(), prescriptionId, userId);

        return itemMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PrescriptionItemResponseDto getItemById(Long itemId) {
        PrescriptionItem item = findItemOrThrow(itemId);
        requireCanView(item.getPrescription());
        return itemMapper.toResponseDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PrescriptionItemResponseDto> getItemsByPrescriptionId(Long prescriptionId) {
        Prescription prescription = findPrescriptionOrThrow(prescriptionId);

       requireCanView(prescription);

        return itemRepository.findAllByPrescriptionIdWithMedication(prescriptionId).stream()
                .map(itemMapper::toResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public PrescriptionItemResponseDto updateItem(Long itemId, PrescriptionItemUpdateRequestDto request) {
        Long userId = currentDoctorUserId();

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
        Long userId = currentDoctorUserId();

        PrescriptionItem item = findItemOrThrow(itemId);
        requireCanModify(item.getPrescription(), userId);

        item.softDelete(userId, LocalDateTime.now(clock));
        itemRepository.flush();


        log.info("Prescription item {} soft deleted by user {}", itemId, userId);
    }

//helpers

    private Long currentDoctorUserId() {
        accessPolicy.requireDoctorRole("modify");
        return securityUtil.getCurrentUserId();
    }


    private PrescriptionItem saveOrTranslateDuplicate(PrescriptionItem item, Long prescriptionId, Long medicationId) {
        try {
            return itemRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException ex) {
            if (ConstraintViolationClassifier.isUniqueViolationOn(ex, ACTIVE_ITEM_UNIQUE_INDEX)) {
                throw new ActiveItemAlreadyExistsException(prescriptionId, medicationId, ex);
            }
            throw ex;
        }
    }

    private static AlreadyExistException duplicateItem(Long prescriptionId, Long medicationId) {
        return new AlreadyExistException(
                "Medication with id " + medicationId + " is already part of prescription " + prescriptionId);
    }

    private void requireCanModify(Prescription prescription, Long userId) {
        accessPolicy.requireCanModify(prescription.getMedicalRecord(), userId,
                () -> "You are not allowed to modify items of prescription with id " + prescription.getId());
    }

    private void requireCanView(Prescription prescription) {
        accessPolicy.requireCanView(prescription.getMedicalRecord(), securityUtil.getCurrentUserId());
    }

    private Prescription findPrescriptionOrThrow(Long prescriptionId) {
        return prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(prescriptionId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + prescriptionId));
    }

    private PrescriptionItem findItemOrThrow(Long itemId) {
        return itemRepository.findByIdWithContext(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription item not found with id: " + itemId));
    }

}