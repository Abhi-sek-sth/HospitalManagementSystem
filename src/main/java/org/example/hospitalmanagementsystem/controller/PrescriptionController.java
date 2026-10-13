package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionResponseDto;
import org.example.hospitalmanagementsystem.service.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> createPrescription(@Valid @RequestBody PrescriptionRequestDto request) {

        PrescriptionResponseDto response = prescriptionService.createPrescription(request);

        return ResponseEntity.ok(ApiResponse.success("Prescription created successfully", response));
    }

    @PreAuthorize("hasAnyRole('ROLE_PATIENT', 'ROLE_DOCTOR')")
   @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> getPrescriptionById(@PathVariable Long id) {

        PrescriptionResponseDto response = prescriptionService.getPrescriptionById(id);

        return ResponseEntity.ok(ApiResponse.success("Prescription fetched successfully by Prescription id", response));
    }

    @PreAuthorize("hasAnyRole('ROLE_PATIENT', 'ROLE_DOCTOR')")
    @GetMapping("/medicalRecordId/{id}")
    public ResponseEntity<ApiResponse<PageResponseDto<PrescriptionResponseDto>>> getPrescriptionsByMedicalRecordId(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        PageResponseDto<PrescriptionResponseDto> response = prescriptionService.getPrescriptionsByMedicalRecordId(id,page,size);

        return ResponseEntity.ok(ApiResponse.success("Prescription fetched successfully by MedicalRecord id", response));
    }

    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> update(@PathVariable Long id,@Valid @RequestBody PrescriptionUpdateRequestDto request) {

        PrescriptionResponseDto response = prescriptionService.updatePrescription(id,request);

        return ResponseEntity.ok(ApiResponse.success("Prescription updated successfully", response));
    }

    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

       prescriptionService.deletePrescription(id);

        return ResponseEntity.ok(ApiResponse.success("Prescription deleted successfully", null));
    }

}
