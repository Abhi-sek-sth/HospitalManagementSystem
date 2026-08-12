package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record DoctorRequestDto(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @NotBlank(message = "Specialization is required")
        @Size(max = 100, message = "Specialization must not exceed 100 characters")
        String specialization,

        @NotBlank(message = "License number is required")
        @Size(max = 50, message = "License number must not exceed 50 characters")
        String licenseNumber,

        Boolean active
) {
}
