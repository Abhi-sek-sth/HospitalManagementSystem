package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.*;

public record PrescriptionItemRequestDto(

        @NotNull(message = "Medication id is required")
        @Positive(message = "Medication id must be positive")
        Long medicationId,

        @NotBlank(message = "Dosage is required")
        @Size(max = 100, message = "Dosage must not exceed 100 characters")
        String dosage,

        @NotBlank(message = "Frequency is required")
        @Size(max = 100, message = "Frequency must not exceed 100 characters")
        String frequency,

        @NotNull(message = "Duration in days is required")
        @Positive(message = "Duration must be at least 1 day")
        @Max(value = 365, message = "Duration must not exceed 365 days")
        Integer durationDays,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be at least 1")
        @Max(value = 1000, message = "Quantity must not exceed 1000")
        Integer quantity,

        @Size(max = 500, message = "Instructions must not exceed 500 characters")
        String instructions
) {}