package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.MedicalRecordUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicalRecordsRequestDto;
import org.example.hospitalmanagementsystem.dto.response.*;
import org.example.hospitalmanagementsystem.service.MedicalRecordsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medicalrecord")
public class MedicalRecordsController {
    private final MedicalRecordsService medicalRecordsService;
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordsResponseDto>> createMedicalRecords(@Valid @RequestBody MedicalRecordsRequestDto medicalRecordsRequestDto){
        MedicalRecordsResponseDto response = medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("MedicalRecord created successfully.",response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR','ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<MedicalRecordsResponseDto>> getById(@PathVariable Long id){
        MedicalRecordsResponseDto responseDto = medicalRecordsService.getMedicalRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Medical Record Retrieve Successfully",responseDto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordsResponseDto>> update(@PathVariable Long id, @Valid @RequestBody MedicalRecordUpdateRequestDto medicalRecordsRequestDto) {
        MedicalRecordsResponseDto responseDto = medicalRecordsService.updateMedicalRecord(id,medicalRecordsRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Medical Record Update Successfully",responseDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
        medicalRecordsService.deleteMedicalRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Medical Record Delete Successfully",null));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponseDto<MedicalRecordsResponseDto>>> getAllRecords(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        PageResponseDto<MedicalRecordsResponseDto> responseDto = medicalRecordsService.getAllMedicalRecords(page,size);
        return ResponseEntity.ok(ApiResponse.success("Successfully Retrieved all Medical Records",responseDto));
    }

    @GetMapping("/byAppointmentId/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<MedicalRecordsResponseDto>> getByAppointmentId(@PathVariable Long id){
        MedicalRecordsResponseDto responseDto = medicalRecordsService.getMedicalRecordByAppointmentId(id);
        return ResponseEntity.ok(ApiResponse.success("Successfully Retrieved  Medical Records by Appointment Id",responseDto));
    }


}
