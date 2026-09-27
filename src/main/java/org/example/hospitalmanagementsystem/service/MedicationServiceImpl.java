package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.hospitalmanagementsystem.dto.request.MedicationRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicationUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.MedicationResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.entity.Medication;
import org.example.hospitalmanagementsystem.exception.AccessDeniedExceptionHandler;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.InvalidOperationException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.MedicationMapper;
import org.example.hospitalmanagementsystem.repository.MedicationRepository;
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
public class MedicationServiceImpl implements MedicationService {


    private static final int MAX_PAGE_SIZE = 100;
    private final MedicationRepository medicationRepository;
    private final MedicationMapper medicationMapper;
    private final SecurityUtil securityUtil;

    @Transactional
    @Override
    public MedicationResponseDto createMedication(MedicationRequestDto request) {
        if(!securityUtil.isAdmin()){
            throw new AccessDeniedExceptionHandler("You are not allowed to create or save medicine");
        }
        assertNoDuplicate(request.name(), request.manufacturer());

        Medication medication = medicationMapper.toEntity(request);

        medication.setActive(true);

        Medication savedMedication = medicationRepository.save(medication);

        return medicationMapper.toResponse(savedMedication);
    }

    @Transactional(readOnly = true)
    @Override
    public MedicationResponseDto getMedicationById(Long id) {

        Medication medication = medicationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

        if (!medication.isActive() && !securityUtil.isAdmin()){
            throw new AccessDeniedExceptionHandler("Currently we don't have this medicine in our store");
        }

        return medicationMapper.toResponse(medication);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponseDto<MedicationResponseDto> getAllActiveMedications(int page,int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());

        Page<Medication> medications = medicationRepository.findByActiveTrue(pageable);
        return new PageResponseDto<>(
                medications.getContent()
                .stream()
                .map(medicationMapper::toResponse)
                .toList(),
                medications.getNumber(),
                medications.getTotalPages(),
                medications.getTotalElements()
        );
    }



    @Transactional
    @Override
    public MedicationResponseDto updateMedication(Long id, MedicationUpdateRequestDto request) {
        if(!securityUtil.isAdmin()){
            throw new AccessDeniedExceptionHandler("You are not allowed to update medication");
        }

        Medication medication = medicationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

        if (request.name() == null && request.manufacturer() == null && request.unitPrice() == null) {

            throw new InvalidOperationException("At least one field must be provided for update");
        }

        String newName = request.name() != null ? request.name() : medication.getName();

        String newManufacturer = request.manufacturer() != null ? request.manufacturer() : medication.getManufacturer();

        boolean nameChanged = request.name() != null && !medication.getName().equalsIgnoreCase(request.name());

        boolean manufacturerChanged = request.manufacturer() != null && !medication.getManufacturer().equalsIgnoreCase(request.manufacturer());

        if (nameChanged || manufacturerChanged) {
            assertNoDuplicate(newName, newManufacturer);
        }


        medicationMapper.updateEntity(request, medication);

        Medication updatedMedication = medicationRepository.save(medication);

        return medicationMapper.toResponse(updatedMedication);
    }

    @Transactional
    @Override
    public void deactivateMedication(Long id) {
        if(!securityUtil.isAdmin()){
            throw new AccessDeniedExceptionHandler("You are not allowed to deactivate medication");
        }

        Medication medication = medicationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

        medication.setActive(false);

        medicationRepository.save(medication);

        log.info("Medication deactivated for :id={} ", id);
    }

    @Transactional
    @Override
    public void activateMedication(Long id) {

        if(!securityUtil.isAdmin()){
            throw new AccessDeniedExceptionHandler("You are not allowed to activate medication");
        }

        Medication medication = medicationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

        medication.setActive(true);

        medicationRepository.save(medication);

        log.info("Medication activate for :id={} ", id);    }

    @Transactional(readOnly = true)
    @Override
    public List<MedicationResponseDto> searchActiveMedications(String name) {
        if(name==null || name.isBlank()){
            throw new InvalidOperationException("Medication name must not be empty");
        }
        return medicationRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream()
                .map(medicationMapper::toResponse)
                .toList();
    }

    private void assertNoDuplicate(String name, String manufacturer) {
        boolean exists = medicationRepository.existsByNameIgnoreCaseAndManufacturerIgnoreCase(name, manufacturer);
        if (exists) {
            throw new AlreadyExistException("Medication already exists with name: " + name + " and manufacturer: " + manufacturer);
        }
    }
}
