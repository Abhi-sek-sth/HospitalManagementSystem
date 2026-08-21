package org.example.hospitalmanagementsystem.dto.response;

import org.example.hospitalmanagementsystem.entity.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponseDto(
        Long id,

        Long patientId,

        Long doctorId,

        LocalDate appointmentDate,

        LocalTime appointmentTime,

        String reason,

        AppointmentStatus status,

        String notes,

        Instant createdAt,

        Instant updatedAt
) {
}
