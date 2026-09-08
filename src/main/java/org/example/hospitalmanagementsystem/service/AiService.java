package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.AiRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AiResponseDto;

public interface AiService {
    AiResponseDto getMessage(AiRequestDto question);
}
