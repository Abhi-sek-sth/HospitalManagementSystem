package org.example.hospitalmanagementsystem.dto.request;

import java.math.BigDecimal;

public record MedicationUpdateRequestDto(
        String name,
        String manufacturer,
        BigDecimal unitPrice

        ) {
}
