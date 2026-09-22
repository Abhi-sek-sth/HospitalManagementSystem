package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.MedicalRecordsRequestDto;
import org.example.hospitalmanagementsystem.dto.response.MedicalRecordsResponseDto;
import org.example.hospitalmanagementsystem.entity.MedicalRecords;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalRecordsMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    MedicalRecords toEntity(MedicalRecordsRequestDto dto);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "patientId", source = "appointment.patient.user.id")
    @Mapping(target = "doctorId", source = "appointment.doctor.user.id")
    MedicalRecordsResponseDto toResponseDto(MedicalRecords medicalRecord);
}
