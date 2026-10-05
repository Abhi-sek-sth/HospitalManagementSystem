package org.example.hospitalmanagementsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record PrescriptionResponseDto(
        Long id,

        Long medicalRecordId,

        String instructions,

        LocalDateTime prescribedAt
) {
}
