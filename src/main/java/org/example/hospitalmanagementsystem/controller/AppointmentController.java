package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.AppointmentRequestDto;
import org.example.hospitalmanagementsystem.dto.request.AppointmentUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.*;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;
import org.example.hospitalmanagementsystem.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final AppointmentService appointmentService;
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPTIONIST','ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> createAppointment(@Valid @RequestBody AppointmentRequestDto appointmentRequestDto){
        AppointmentResponseDto response = appointmentService.create(appointmentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Appointment created successfully.",response));
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentUpdateRequestDto appointmentRequestDto){
        AppointmentResponseDto response = appointmentService.update(id,appointmentRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Appointment updated successfully",response));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> getById(@PathVariable Long id){
        AppointmentResponseDto response = appointmentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved successfully",response));
    }
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentPageResponseDto<AppointmentResponseDto>>> getAllAppointments(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        AppointmentPageResponseDto<AppointmentResponseDto> responseDto =appointmentService.getAll(page,size);
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved all appointments",responseDto));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable Long id){appointmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment deleted successfully",null));
    }
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPTIONIST','ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponseDto>>> getByPatient(@PathVariable Long patientId, @PageableDefault(size = 10, sort = "appointmentDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AppointmentResponseDto> response = appointmentService.getByPatient(patientId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Patient appointments retrieved successfully.", response));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPTIONIST','ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponseDto>>> getByDoctor(@PathVariable Long doctorId, @PageableDefault(size = 10, sort = "appointmentDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AppointmentResponseDto> response = appointmentService.getByDoctor(doctorId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Doctor appointments retrieved successfully.", response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> cancel(@PathVariable Long id) {
        AppointmentResponseDto appointmentResponseDto = appointmentService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success("Successfully update status",appointmentResponseDto));
    }
    @PatchMapping("/{id}/{newStatus}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> changeStatus(@PathVariable Long id, @PathVariable AppointmentStatus newStatus) {
        AppointmentResponseDto appointmentResponseDto = appointmentService.updateAppointmentStatus(id,newStatus);
        return ResponseEntity.ok(ApiResponse.success("Successfully update status",appointmentResponseDto));
    }
}
