package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.MedicalRecordsRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorResponseDto;
import org.example.hospitalmanagementsystem.dto.response.MedicalRecordsResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;

import java.util.List;

public interface MedicalRecordsService {
    MedicalRecordsResponseDto createMedicalRecord(MedicalRecordsRequestDto request);

    MedicalRecordsResponseDto getMedicalRecordById(Long id);

    MedicalRecordsResponseDto getMedicalRecordByAppointmentId(Long appointmentId);

    PageResponseDto<MedicalRecordsResponseDto> getAllMedicalRecords(int page, int size);

    MedicalRecordsResponseDto updateMedicalRecord(Long id, MedicalRecordsRequestDto request);

    void deleteMedicalRecord(Long id);


}
