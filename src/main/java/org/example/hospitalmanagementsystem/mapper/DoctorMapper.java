package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.DoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorResponseDto;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Doctor toEntity(DoctorRequestDto requestDto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "departmentId", source = "department.id")
    DoctorResponseDto toResponseDto(Doctor doctor);
}