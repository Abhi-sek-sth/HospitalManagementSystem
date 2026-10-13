package org.example.hospitalmanagementsystem.dto.response;

import java.time.Instant;

public record PrescriptionItemResponseDto(
        Long id,
        Long prescriptionId,
        Long medicationId,
        String medicationName,
        String medicationManufacturer,   // name + manufacturer is the identity of a medication in your system
        String dosage,
        String frequency,
        Integer durationDays,
        Integer quantity,
        String instructions,
        Instant createdAt,
        Instant updatedAt
) {}