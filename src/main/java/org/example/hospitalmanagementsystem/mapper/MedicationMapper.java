package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.MedicationRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicationUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.MedicationResponseDto;
import org.example.hospitalmanagementsystem.entity.Medication;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MedicationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Medication toEntity(MedicationRequestDto request);

    MedicationResponseDto toResponse(Medication medication);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(MedicationUpdateRequestDto request, @MappingTarget Medication medication);
}
