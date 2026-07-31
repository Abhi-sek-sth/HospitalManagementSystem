package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
         String email,

                @NotBlank(message = "Username is required")
@Size(min = 4, max = 30, message = "Username must be between 4 and 30 characters")
 String username,

@NotBlank(message = "Password is required")
@Size(min = 8, max = 100, message = "Password must be at least 8 characters")
@Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain uppercase, lowercase, number and special character"
)
 String password,

@NotBlank(message = "First name is required")
@Size(max = 50)
 String firstName,

@NotBlank(message = "Last name is required")
@Size(max = 50)
 String lastName,

@NotBlank(message = "Role is required")
 String role
) {
}
