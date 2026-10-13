package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.DoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.request.UpdateDoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.dto.response.DoctorPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorResponseDto;
import org.example.hospitalmanagementsystem.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;


@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> createDoctor(@Valid @RequestBody DoctorRequestDto doctorRequestDto){
        DoctorResponseDto doctorResponseDto = doctorService.create(doctorRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Doctor Created Successfully",doctorResponseDto));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> getById(@PathVariable Long id){
        DoctorResponseDto doctorResponseDto = doctorService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor Retrieve Successfully",doctorResponseDto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> update(@PathVariable Long id, @Valid @RequestBody UpdateDoctorRequestDto updateDoctorRequestDto) {
        DoctorResponseDto doctorResponseDto = doctorService.update(id,updateDoctorRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Doctor Update Successfully",doctorResponseDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
        doctorService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor Delete Successfully",null));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DoctorPageResponseDto<DoctorResponseDto>>> getAllDoctors(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        DoctorPageResponseDto<DoctorResponseDto> doctorResponseDtoDoctorPageResponseDto = doctorService.getAll(page,size);
        return ResponseEntity.ok(ApiResponse.success("Successfully Retrieved all Doctors",doctorResponseDtoDoctorPageResponseDto));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {

        doctorService.activate(id);

        return ResponseEntity.ok(ApiResponse.success( "Doctor activated successfully", null)
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {

        doctorService.deactivate(id);

        return ResponseEntity.ok(ApiResponse.success( "Doctor deactivated successfully", null)
        );
    }



}
