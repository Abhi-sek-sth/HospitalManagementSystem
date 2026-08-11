package org.example.hospitalmanagementsystem.dto.request;

public record UpdateDoctorRequestDto(
        String phone,
        Long departmentId,
        String specialization,
        Boolean active
) {
}
