package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.AppointmentRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Appointment toEntity(AppointmentRequestDto dto);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "doctorId", source = "doctor.id")
    AppointmentResponseDto toResponseDto(Appointment appointment);
}
