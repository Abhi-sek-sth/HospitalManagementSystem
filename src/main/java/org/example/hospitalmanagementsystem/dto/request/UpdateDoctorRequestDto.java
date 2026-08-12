package org.example.hospitalmanagementsystem.dto.request;

import lombok.Builder;

@Builder
public record UpdateDoctorRequestDto(
        String phone,
        Long departmentId,
        String specialization,
        Boolean active
) {
}
