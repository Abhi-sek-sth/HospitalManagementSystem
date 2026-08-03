package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.LoginRequestDto;
import org.example.hospitalmanagementsystem.dto.request.RefreshTokenRequestDto;
import org.example.hospitalmanagementsystem.dto.response.LoginResponseDto;
import org.example.hospitalmanagementsystem.dto.response.RefreshTokenResponseDto;

public interface LoginService {
    LoginResponseDto logIn(LoginRequestDto loginRequestDto);
    RefreshTokenResponseDto createNewAccessToken(RefreshTokenRequestDto refreshToken);
}
