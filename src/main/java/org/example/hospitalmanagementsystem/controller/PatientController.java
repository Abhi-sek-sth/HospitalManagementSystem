package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.PatientRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PatientUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PatientPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PatientResponseDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> createPatient(@Valid @RequestBody PatientRequestDto patientRequestDto) {

        PatientResponseDto patientResponseDto = patientService.create(patientRequestDto);

        return ResponseEntity.ok(ApiResponse.success("Successfully Created Patient", patientResponseDto));
    }


    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientPageResponseDto<PatientResponseDto>>> getAllPatients(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        PatientPageResponseDto<PatientResponseDto> patientPageResponseDto = patientService.getAll(page, size);

        return ResponseEntity.ok(ApiResponse.success("Successfully Retrieved all Patients", patientPageResponseDto));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> getPatientById(@PathVariable Long id) {

        PatientResponseDto patientResponseDto = patientService.getById(id);

        return ResponseEntity.ok(ApiResponse.success("Successfully Retrieved Patient", patientResponseDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequestDto patientUpdateRequestDto) {

        PatientResponseDto patientResponseDto = patientService.update(id, patientUpdateRequestDto);

        return ResponseEntity.ok(ApiResponse.success("Successfully Updated Patient", patientResponseDto));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {

        patientService.delete(id);

        return ResponseEntity.ok(ApiResponse.success("Successfully Deleted Patient", null));
    }
}