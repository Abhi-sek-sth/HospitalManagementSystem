package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.Size;

public record PrescriptionUpdateRequestDto(

        @Size(
                max = 2000,
                message = "Instructions cannot exceed 2000 characters"
        )
        String instructions

) {
}
