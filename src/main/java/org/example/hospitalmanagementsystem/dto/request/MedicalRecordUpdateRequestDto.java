package org.example.hospitalmanagementsystem.dto.request;

public record MedicalRecordUpdateRequestDto(
        String diagnosis,
        String treatment,
        String symptoms,
        String notes
) {
}
