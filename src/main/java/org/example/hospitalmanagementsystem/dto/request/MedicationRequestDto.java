package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MedicationRequestDto(
        @NotBlank(message = "Medication name is required")
        @Size(max = 150, message = "Medication name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Manufacturer is required")
        @Size(max = 150, message = "Manufacturer must not exceed 150 characters")
        String manufacturer,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Unit price cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Unit price must have at most 8 integer digits and 2 decimal places")
        BigDecimal unitPrice
) {
}
