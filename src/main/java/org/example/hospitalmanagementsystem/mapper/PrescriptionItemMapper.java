package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionItemResponseDto;
import org.example.hospitalmanagementsystem.entity.PrescriptionItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PrescriptionItemMapper {

    // Relationships (prescription, medication) are NOT mapped: the service sets real, loaded entities.
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "dosage", source = "dosage")
    @Mapping(target = "frequency", source = "frequency")
    @Mapping(target = "durationDays", source = "durationDays")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "instructions", source = "instructions")
    PrescriptionItem toEntity(PrescriptionItemRequestDto request);

    @Mapping(target = "prescriptionId", source = "prescription.id")
    @Mapping(target = "medicationId", source = "medication.id")
    @Mapping(target = "medicationName", source = "medication.name")
    @Mapping(target = "medicationManufacturer", source = "medication.manufacturer")
    PrescriptionItemResponseDto toResponseDto(PrescriptionItem item);

    @BeanMapping(ignoreByDefault = true,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dosage", source = "dosage")
    @Mapping(target = "frequency", source = "frequency")
    @Mapping(target = "durationDays", source = "durationDays")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "instructions", source = "instructions")
    void updateEntity(PrescriptionItemUpdateRequestDto request, @MappingTarget PrescriptionItem item);
}