package org.example.hospitalmanagementsystem.dto.response;

import java.time.LocalDateTime;

public record MedicalRecordsResponseDto(
         Long id,

         Long appointmentId,

         Long patientId,

         Long doctorId,

         String diagnosis,

         String symptoms,

         String treatment,

         String notes,

         LocalDateTime createdAt,

         LocalDateTime updatedAt
) {
}
