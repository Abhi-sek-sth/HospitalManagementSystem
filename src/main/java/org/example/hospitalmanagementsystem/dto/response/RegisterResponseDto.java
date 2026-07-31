package org.example.hospitalmanagementsystem.dto.response;

import java.time.LocalDateTime;

public record RegisterResponseDto(
         Long id,

         String email,

         String username,

        String firstName,

         String lastName,

         String role,

         LocalDateTime createdAt
) {
}
