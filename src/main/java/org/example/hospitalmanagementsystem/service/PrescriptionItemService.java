package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionItemResponseDto;

import java.util.List;

public interface PrescriptionItemService {

    PrescriptionItemResponseDto addItem(Long prescriptionId, PrescriptionItemRequestDto request);

    PrescriptionItemResponseDto getItemById(Long itemId);

    List<PrescriptionItemResponseDto> getItemsByPrescriptionId(Long prescriptionId);

    PrescriptionItemResponseDto updateItem(Long itemId, PrescriptionItemUpdateRequestDto request);

    void deleteItem(Long itemId);
}