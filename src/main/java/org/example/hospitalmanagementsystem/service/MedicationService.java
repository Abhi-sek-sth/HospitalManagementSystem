package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.MedicalRecordUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicationRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicationUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.MedicationResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;

import java.util.List;

public interface MedicationService {
    MedicationResponseDto createMedication(MedicationRequestDto request);

    MedicationResponseDto getMedicationById(Long id);

    PageResponseDto<MedicationResponseDto> getAllActiveMedications(int page, int size);

    MedicationResponseDto updateMedication(Long id, MedicationUpdateRequestDto request);

    void deactivateMedication(Long id);

    void activateMedication(Long id);

    List<MedicationResponseDto> searchActiveMedications(String name);
}
