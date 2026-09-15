package org.example.hospitalmanagementsystem.service;


import org.example.hospitalmanagementsystem.dto.response.DoctorRecommendationResponseDto;

public interface RecommendationService {
    DoctorRecommendationResponseDto recommendSpecialization(Long id);
}
