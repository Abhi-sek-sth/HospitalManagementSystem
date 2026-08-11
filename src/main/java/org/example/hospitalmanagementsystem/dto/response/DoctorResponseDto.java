package org.example.hospitalmanagementsystem.dto.response;

import lombok.Builder;

import java.time.Instant;
@Builder
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
