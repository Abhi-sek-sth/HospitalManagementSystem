package org.example.hospitalmanagementsystem.dto.response;

public record LoginResponseDto(
        String Token,
        String refreshToken
) {
}
