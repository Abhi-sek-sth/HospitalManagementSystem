package org.example.hospitalmanagementsystem.dto.request;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record MedicationUpdateRequestDto(
        String name,
        String manufacturer,
        BigDecimal unitPrice

        ) {
}
