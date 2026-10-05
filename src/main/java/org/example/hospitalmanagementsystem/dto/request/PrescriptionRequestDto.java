package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PrescriptionRequestDto(
        @NotNull(message = "Medical record ID is required")
        @Positive(message = "Medical record ID must be greater than 0")
        Long medicalRecordId,

        @Size(
                max = 2000,
                message = "Instructions cannot exceed 2000 characters"
        )
        String instructions
) {
}
