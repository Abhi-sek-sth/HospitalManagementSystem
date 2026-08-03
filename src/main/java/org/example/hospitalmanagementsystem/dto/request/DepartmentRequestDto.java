package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequestDto(
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
         String name,

        @NotBlank(message = "Location is required")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}
