package org.example.hospitalmanagementsystem.service;


import org.example.hospitalmanagementsystem.dto.request.HospitalInfoRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.HospitalInfoResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.entity.InformationCategory;



import java.util.List;

public interface HospitalInfoService {
    HospitalInfoResponseDto create(HospitalInfoRequestDto requestDto);

    HospitalInfoResponseDto getById(Long id);

    PageResponseDto<HospitalInfoResponseDto> getAll(int page, int size);

    List<HospitalInfoResponseDto> getByCategory(InformationCategory category);

    HospitalInfoResponseDto update(Long id, HospitalInfoRequestDto requestDto);

    void deactivate(Long id);

    void activate(Long id);

    void delete(Long id);
}
