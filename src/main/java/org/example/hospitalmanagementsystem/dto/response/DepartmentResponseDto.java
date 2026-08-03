package org.example.hospitalmanagementsystem.dto.response;

import java.time.LocalDateTime;

public record DepartmentResponseDto(

         Long id,
         String name,
         String location,
         String description,
         LocalDateTime createdAt
) {
}
