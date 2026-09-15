package org.example.hospitalmanagementsystem.service;


import org.example.hospitalmanagementsystem.dto.response.DoctorRecommendationResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;

public interface RecommendationService {
//    DoctorRecommendationResponseDto searchRecommendation(Long id);
    DoctorRecommendationResponseDto recommendSpecialization(Long id);
}
