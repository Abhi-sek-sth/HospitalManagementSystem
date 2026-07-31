package org.example.hospitalmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.RegisterRequestDto;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.RegisterResponseDto;
import org.example.hospitalmanagementsystem.service.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;
    @PostMapping
    public ResponseEntity<ApiResponse<RegisterResponseDto>> createUser(@Valid @RequestBody RegisterRequestDto registerRequestDto){
        RegisterResponseDto registerResponseDto = registerService.register(registerRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Successfully save a user",registerResponseDto));
    }

    @GetMapping("/allUsers")
    public ResponseEntity<ApiResponse<PageResponseDto<RegisterResponseDto>>> getUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResponseDto<RegisterResponseDto> allUsers = registerService.getAllUsers(page,size);
        return ResponseEntity.ok(ApiResponse.success("Users are fetched",allUsers));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id){
        registerService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("user delete successfully",null));

    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> getById(@PathVariable Long id){
        RegisterResponseDto registerResponseDto = registerService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("A User is fetched",registerResponseDto));
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
@PatchMapping("/updateUser/{id}")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> updateUser(@Valid @RequestBody RegisterRequestDto registerRequestDto, @PathVariable Long id ){
RegisterResponseDto registerResponseDto = registerService.update(registerRequestDto,id);
return ResponseEntity.ok(ApiResponse.success("Successfully update user",registerResponseDto));


    }
}
