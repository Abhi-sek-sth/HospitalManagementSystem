package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MedicalRecordsRequestDto(
        @NotNull(message = "Appointment id is required")
        @Positive(message = "Appointment id must be positive")
         Long appointmentId,

        @NotBlank(message = "Diagnosis is required")
        @Size(max = 5000, message = "Diagnosis must not exceed 5000 characters")
        String diagnosis,


        @Size(max = 5000, message = "Symptoms must not exceed 5000 characters")
         String symptoms,

        @Size(max = 5000, message = "Treatment must not exceed 5000 characters")
        String treatment,

        @Size(max = 5000, message = "Notes must not exceed 5000 characters")
        String notes
) {
}
