package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.DepartmentRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {
    DepartmentResponseDto create(DepartmentRequestDto requestDto);

    DepartmentResponseDto update(Long id, DepartmentRequestDto requestDto);

    DepartmentResponseDto getById(Long id);

    DepartmentPageResponseDto<DepartmentResponseDto> getAll(int page,int size);

    void delete(Long id);
}
