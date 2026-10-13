package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.PrescriptionRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionResponseDto;

import java.util.List;

public interface PrescriptionService {
    PrescriptionResponseDto createPrescription(PrescriptionRequestDto request);

    PrescriptionResponseDto getPrescriptionById(Long prescriptionId);

    PageResponseDto<PrescriptionResponseDto> getPrescriptionsByMedicalRecordId(Long medicalRecordId,int page,int size);

    PrescriptionResponseDto updatePrescription(Long prescriptionId, PrescriptionUpdateRequestDto request);

    void deletePrescription(Long prescriptionId);
}
