package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.DoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.request.UpdateDoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorResponseDto;

public interface DoctorService {
    DoctorResponseDto create(DoctorRequestDto requestDto);

    DoctorResponseDto getById(Long id);

    DoctorResponseDto update(Long id, UpdateDoctorRequestDto requestDto);

    void delete(Long id);

    DoctorPageResponseDto<DoctorResponseDto> getAll(int page, int size);

    void deactivate(Long id);

    void activate(Long id);
}
