package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.DepartmentRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentResponseDto;
import org.example.hospitalmanagementsystem.entity.Department;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    Department toEntity(DepartmentRequestDto dto);
    DepartmentResponseDto toResponse(Department entity);
}
