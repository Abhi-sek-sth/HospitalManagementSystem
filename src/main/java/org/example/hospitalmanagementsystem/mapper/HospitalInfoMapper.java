package org.example.hospitalmanagementsystem.mapper;

import org.example.hospitalmanagementsystem.dto.request.HospitalInfoRequestDto;
import org.example.hospitalmanagementsystem.dto.response.HospitalInfoResponseDto;
import org.example.hospitalmanagementsystem.entity.HospitalInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HospitalInfoMapper {
    HospitalInfo toEntity(HospitalInfoRequestDto requestDto);

    HospitalInfoResponseDto toResponseDto(HospitalInfo hospitalInfo);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromDto(HospitalInfoRequestDto requestDto, @MappingTarget HospitalInfo hospitalInfo);
}
