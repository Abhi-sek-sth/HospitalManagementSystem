package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.hospitalmanagementsystem.dto.request.MedicationRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicationUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.dto.response.MedicationResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.service.MedicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicationResponseDto>> createMedication(@Valid @RequestBody MedicationRequestDto request) {

        MedicationResponseDto response = medicationService.createMedication(request);

        return ResponseEntity.ok(ApiResponse.success("Medication created successfully", response));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicationResponseDto>> getMedicationById(@PathVariable Long id) {

        MedicationResponseDto response = medicationService.getMedicationById(id);

        return ResponseEntity.ok(ApiResponse.success("Medication fetched successfully", response));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<MedicationResponseDto>>> getAllActiveMedications(@Valid @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        PageResponseDto<MedicationResponseDto> response = medicationService.getAllActiveMedications(page, size);

        return ResponseEntity.ok(ApiResponse.success("Medications fetched successfully", response));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicationResponseDto>> updateMedication(@PathVariable Long id, @Valid @RequestBody MedicationUpdateRequestDto request) {

        MedicationResponseDto response = medicationService.updateMedication(id, request);

        return ResponseEntity.ok(ApiResponse.success("Medication updated successfully", response));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateMedication(@PathVariable Long id) {

        medicationService.deactivateMedication(id);

        return ResponseEntity.ok(ApiResponse.success("Medication deactivated successfully", null));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateMedication(@PathVariable Long id) {

        medicationService.activateMedication(id);

        return ResponseEntity.ok(ApiResponse.success("Medication activated successfully", null));
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MedicationResponseDto>>> getByName(@RequestParam String name) {

        List<MedicationResponseDto> responseDto =medicationService.searchActiveMedications(name);

        return ResponseEntity.ok(ApiResponse.success("Medication fetched successfully", responseDto));
    }
}