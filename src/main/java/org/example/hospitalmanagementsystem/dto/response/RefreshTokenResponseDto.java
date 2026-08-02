package org.example.hospitalmanagementsystem.dto.response;

public record RefreshTokenResponseDto(
        String newAccessToken,
        String newRefreshToken
) {
}
