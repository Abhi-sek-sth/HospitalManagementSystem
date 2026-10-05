package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.PrescriptionRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionResponseDto;
import org.example.hospitalmanagementsystem.entity.Prescription;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "prescribedAt", ignore = true)
    Prescription toEntity(PrescriptionRequestDto request);

    @Mapping(target = "medicalRecordId", source = "medicalRecord.id")
    PrescriptionResponseDto toResponseDto(Prescription prescription);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "prescribedAt", ignore = true)
    void updateEntity(PrescriptionUpdateRequestDto request, @MappingTarget Prescription prescription
    );
}