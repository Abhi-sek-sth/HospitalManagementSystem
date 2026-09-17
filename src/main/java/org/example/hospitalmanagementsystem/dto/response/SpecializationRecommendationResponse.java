package org.example.hospitalmanagementsystem.dto.response;

import java.util.List;

public record SpecializationRecommendationResponse(
         String recommendedSpecialization,
         boolean departmentAvailable,
         List<DoctorResponseDto> recommendedDoctors,
         String message
) {
}
