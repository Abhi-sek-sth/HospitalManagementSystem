package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PrescriptionItemUpdateRequestDto(
        @Pattern(regexp = ".*\\S.*", message = "Dosage must not be blank")
        @Size(max = 100, message = "Dosage must not exceed 100 characters")
        String dosage,

        @Pattern(regexp = ".*\\S.*", message = "Frequency must not be blank")
        @Size(max = 100, message = "Frequency must not exceed 100 characters")
        String frequency,

        @Positive(message = "Duration must be at least 1 day")
        @Max(value = 365, message = "Duration must not exceed 365 days")
        Integer durationDays,

        @Positive(message = "Quantity must be at least 1")
        @Max(value = 1000, message = "Quantity must not exceed 1000")
        Integer quantity,

        // "" is allowed and clears the instructions (null means "unchanged")
        @Size(max = 500, message = "Instructions must not exceed 500 characters")
        String instructions
) {
    public boolean hasAnyField() {
        return dosage != null || frequency != null || durationDays != null
                || quantity != null || instructions != null;
}}
