package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.hospitalmanagementsystem.entity.InformationCategory;

public record HospitalInfoRequestDto(
        @NotNull(message = "Category is required")
        InformationCategory category,

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        String description
) {
}
