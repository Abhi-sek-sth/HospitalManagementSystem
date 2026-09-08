package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.HospitalInfoRequestDto;
import org.example.hospitalmanagementsystem.dto.response.*;
import org.example.hospitalmanagementsystem.entity.InformationCategory;
import org.example.hospitalmanagementsystem.service.HospitalInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hospitalInfo")
public class HospitalInfoController {
    private final HospitalInfoService hospitalInfoService;
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalInfoResponseDto>> createInfo(@Valid @RequestBody HospitalInfoRequestDto hospitalInfoRequestDto){
        HospitalInfoResponseDto response = hospitalInfoService.create(hospitalInfoRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Information saved successfully.",response));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST','ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<HospitalInfoResponseDto>> getById(@PathVariable Long id){
        HospitalInfoResponseDto responseDto = hospitalInfoService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Hospital information Retrieve Successfully",responseDto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalInfoResponseDto>> update(@PathVariable Long id, @Valid @RequestBody HospitalInfoRequestDto updateHospitalInfo) {
        HospitalInfoResponseDto hospitalInfoResponseDto = hospitalInfoService.update(id,updateHospitalInfo);
        return ResponseEntity.ok(ApiResponse.success("HospitalInfo Update Successfully",hospitalInfoResponseDto));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deActivate(@PathVariable Long id){
        hospitalInfoService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("HospitalInfo Deactivate Successfully",null));
    }

    @GetMapping("/byCategory/{category}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<HospitalInfoResponseDto>>> getByCategory(@PathVariable InformationCategory category){
        List<HospitalInfoResponseDto> hospitalInfoResponseDtoList= hospitalInfoService.getByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Successfully Retrieved all related category information",hospitalInfoResponseDtoList));
    }
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PageResponseDto<HospitalInfoResponseDto>>> getAll( @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        PageResponseDto<HospitalInfoResponseDto> hospitalInfoResponseDtoList= hospitalInfoService.getAll(page,size);
        return ResponseEntity.ok(ApiResponse.success("Successfully Retrieved all related category information",hospitalInfoResponseDtoList));
    }

    @DeleteMapping("/activate/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id){
        hospitalInfoService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("HospitalInfo ReActivate Successfully",null));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
        hospitalInfoService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("HospitalInfo Delete Successfully",null));
    }

}
