package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.LoginRequestDto;
import org.example.hospitalmanagementsystem.dto.request.RefreshTokenRequestDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.dto.response.LoginResponseDto;
import org.example.hospitalmanagementsystem.dto.response.RefreshTokenResponseDto;
import org.example.hospitalmanagementsystem.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/Login")
public class LogInController {
    private final LoginService loginService;
@PostMapping
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto){
        LoginResponseDto loginResponseDto = loginService.logIn(loginRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Successfully Login",loginResponseDto));
    }

   @PostMapping("/refresh")
   public ResponseEntity<ApiResponse<RefreshTokenResponseDto>> generateAccessToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto){
    RefreshTokenResponseDto refreshTokenResponseDto = loginService.createNewAccessToken(refreshTokenRequestDto);
    return ResponseEntity.ok(ApiResponse.success("Successfully create new access token",refreshTokenResponseDto));
   }
}
