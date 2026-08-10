package org.example.hospitalmanagementsystem.dto.response;

import java.time.Instant;

public record DoctorResponseDto(
        Long id,
        Long userId,
        Long departmentId,
        String phone,
        String specialization,
        String licenseNumber,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
