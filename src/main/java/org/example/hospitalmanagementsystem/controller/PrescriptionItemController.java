package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionItemResponseDto;
import org.example.hospitalmanagementsystem.service.PrescriptionItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PrescriptionItemController {

    private final PrescriptionItemService itemService;

    @PostMapping("/prescriptions/{prescriptionId}/items")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionItemResponseDto>> addItem(@PathVariable Long prescriptionId, @Valid @RequestBody PrescriptionItemRequestDto request) {
        PrescriptionItemResponseDto created = itemService.addItem(prescriptionId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added successfully",created));
    }

    @GetMapping("/prescriptions/{prescriptionId}/items")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR','ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<List<PrescriptionItemResponseDto>>> getItems(@PathVariable Long prescriptionId) {
        List<PrescriptionItemResponseDto> responseDto = itemService.getItemsByPrescriptionId(prescriptionId);
        return ResponseEntity.ok(ApiResponse.success("Fetched Items with prescription id successfully",responseDto));
    }

    @GetMapping("/prescription-items/{itemId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR','ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<PrescriptionItemResponseDto>> getItem(@PathVariable Long itemId) {
        PrescriptionItemResponseDto responseDto = itemService.getItemById(itemId);
        return ResponseEntity.ok(ApiResponse.success("Fetched item with PrescriptionItem id successfully",responseDto));
    }

    @PatchMapping("/prescription-items/{itemId}")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionItemResponseDto>> updateItem(@PathVariable Long itemId, @Valid @RequestBody PrescriptionItemUpdateRequestDto request) {
       PrescriptionItemResponseDto responseDto = itemService.updateItem(itemId,request);
        return ResponseEntity.ok(ApiResponse.success("item update successfully",responseDto));
    }

    @DeleteMapping("/prescription-items/{itemId}")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully",null));
    }
}