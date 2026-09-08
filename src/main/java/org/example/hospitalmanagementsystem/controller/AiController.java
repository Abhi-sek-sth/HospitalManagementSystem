package org.example.hospitalmanagementsystem.controller;


import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.AiRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AiResponseDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AiResponseDto>>ask(@RequestBody AiRequestDto request) {
        AiResponseDto aiResponseDto = aiService.getMessage(request);

        return ResponseEntity.ok(ApiResponse.success("Here is a answer of your Question",aiResponseDto));
    }
}
