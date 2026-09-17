package org.example.hospitalmanagementsystem.dto.request;

public record RecommendedDoctorDto(
        Long doctorId,

        String doctorName,

        String specialization,

        String department
) {
}
