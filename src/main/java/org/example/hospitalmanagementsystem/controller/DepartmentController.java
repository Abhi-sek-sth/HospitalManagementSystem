package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.DepartmentRequestDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.dto.response.DepartmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentResponseDto;
import org.example.hospitalmanagementsystem.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> createDepartment(@Valid @RequestBody DepartmentRequestDto departmentRequestDto){
        DepartmentResponseDto response = departmentService.create(departmentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Department created successfully.",response));
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> updateDepartment( @PathVariable Long id,@Valid @RequestBody DepartmentRequestDto departmentRequestDto){
        DepartmentResponseDto response = departmentService.update(id,departmentRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully",response));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> getById(@PathVariable Long id){
        DepartmentResponseDto response = departmentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully",response));
    }
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DepartmentPageResponseDto<DepartmentResponseDto>>> getAllDepartments(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        DepartmentPageResponseDto<DepartmentResponseDto> responseDto =departmentService.getAll(page,size);
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved all departments",responseDto));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id){
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully",null));
    }
}
