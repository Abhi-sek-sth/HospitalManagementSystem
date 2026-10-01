package org.example.hospitalmanagementsystem.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MedicationResponseDto(
        Long id,

        String name,

        String manufacturer,

        BigDecimal unitPrice,

        boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
