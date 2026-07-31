package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.RegisterRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.RegisterResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RegisterService {
    RegisterResponseDto register(RegisterRequestDto requestDto);
    PageResponseDto<RegisterResponseDto> getAllUsers(int page, int size);
    void deleteUser(Long id);
    RegisterResponseDto getById(Long id);
    RegisterResponseDto update(RegisterRequestDto registerRequestDto , Long id);
}
