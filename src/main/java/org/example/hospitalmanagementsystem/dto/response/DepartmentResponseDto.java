package org.example.hospitalmanagementsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record DepartmentResponseDto(

         Long id,
         String name,
         String location,
         String description,
         LocalDateTime createdAt
) {
}
