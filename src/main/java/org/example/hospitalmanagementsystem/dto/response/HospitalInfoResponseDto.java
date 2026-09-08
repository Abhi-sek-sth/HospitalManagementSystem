package org.example.hospitalmanagementsystem.dto.response;

import org.example.hospitalmanagementsystem.entity.InformationCategory;

import java.time.LocalDateTime;

public record HospitalInfoResponseDto(
        Long id,

        InformationCategory category,

        String title,

        String description,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
