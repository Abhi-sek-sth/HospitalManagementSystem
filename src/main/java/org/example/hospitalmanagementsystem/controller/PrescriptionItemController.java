package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionItemResponseDto;
import org.example.hospitalmanagementsystem.service.PrescriptionItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PrescriptionItemController {

    private final PrescriptionItemService itemService;

    @PostMapping("/prescriptions/{prescriptionId}/items")
    public ResponseEntity<PrescriptionItemResponseDto> addItem(
            @PathVariable Long prescriptionId,
            @Valid @RequestBody PrescriptionItemRequestDto request) {

        PrescriptionItemResponseDto created = itemService.addItem(prescriptionId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/prescription-items/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/prescriptions/{prescriptionId}/items")
    public ResponseEntity<List<PrescriptionItemResponseDto>> getItems(@PathVariable Long prescriptionId) {
        return ResponseEntity.ok(itemService.getItemsByPrescriptionId(prescriptionId));
    }

    @GetMapping("/prescription-items/{itemId}")
    public ResponseEntity<PrescriptionItemResponseDto> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItemById(itemId));
    }

    @PatchMapping("/prescription-items/{itemId}")
    public ResponseEntity<PrescriptionItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody PrescriptionItemUpdateRequestDto request) {
        return ResponseEntity.ok(itemService.updateItem(itemId, request));
    }

    @DeleteMapping("/prescription-items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}