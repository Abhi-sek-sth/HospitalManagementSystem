package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

import lombok.Builder;
import org.example.hospitalmanagementsystem.entity.Gender;
@Builder
public record PatientUpdateRequestDto(
        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^(98|97|96)\\d{8}$",
                message = "Phone number must be a valid Nepalese mobile number"
        )
        String phone,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "Gender is required")
        Gender gender,

        @Size(
                max = 255,
                message = "Address must not exceed 255 characters"
        )
        String address,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}