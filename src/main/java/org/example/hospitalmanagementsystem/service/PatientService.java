package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.PatientRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PatientUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PatientResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PatientPageResponseDto;

public interface PatientService {

    PatientResponseDto create(PatientRequestDto requestDto);

    PatientResponseDto getById(Long id);

    PatientPageResponseDto<PatientResponseDto> getAll(int page, int size);

    PatientResponseDto update(Long id, PatientUpdateRequestDto requestDto);

    void delete(Long id);
}