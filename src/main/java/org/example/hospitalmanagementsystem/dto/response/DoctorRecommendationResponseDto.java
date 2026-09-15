package org.example.hospitalmanagementsystem.dto.response;

import lombok.Builder;
import org.example.hospitalmanagementsystem.dto.request.RecommendedDoctorDto;

import java.util.List;
@Builder
public record DoctorRecommendationResponseDto(
        Long appointmentId,

        String problem,

        String bookedDoctorName,

        String bookedDoctorSpecialization,

        String recommendedSpecialization,

        boolean specializationMatches,

        String message,

        List<RecommendedDoctorDto> recommendedDoctor
) {
}
