package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.RegisterRequestDto;
import org.example.hospitalmanagementsystem.dto.response.RegisterResponseDto;
import org.example.hospitalmanagementsystem.entity.Register;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegisterMapper {
    Register toEntity(RegisterRequestDto requestDto);

    RegisterResponseDto toResponseDto(Register register);
}
