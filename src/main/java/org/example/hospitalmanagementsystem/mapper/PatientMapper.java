package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.PatientRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PatientUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PatientResponseDto;
import org.example.hospitalmanagementsystem.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Patient toEntity(PatientRequestDto requestDto);

    @Mapping(target = "userId", source = "user.id")
    PatientResponseDto toResponseDto(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            PatientUpdateRequestDto requestDto,
            @MappingTarget Patient patient
    );
}