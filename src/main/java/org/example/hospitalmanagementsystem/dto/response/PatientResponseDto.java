package org.example.hospitalmanagementsystem.dto.response;

import org.example.hospitalmanagementsystem.entity.Gender;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponseDto(

        Long id,

        Long userId,

        String phone,

        LocalDate dateOfBirth,

        Gender gender,

        String address,

        Boolean active,

        Instant createdAt,

        Instant updatedAt
) {
}