package org.example.hospitalmanagementsystem.controller;


import lombok.RequiredArgsConstructor;

import org.example.hospitalmanagementsystem.dto.response.ApiResponse;

import org.example.hospitalmanagementsystem.dto.response.DoctorRecommendationResponseDto;
import org.example.hospitalmanagementsystem.service.RecommendationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointments")
public class AiRecommendationController {
    private final RecommendationService recommendationService;
    @GetMapping("/{id}/recommendation")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DoctorRecommendationResponseDto>> askProblem(@PathVariable Long id){
        DoctorRecommendationResponseDto response = recommendationService.recommendSpecialization(id);
        return ResponseEntity.ok(ApiResponse.success("Recommendation for your problem",response));
    }
}
