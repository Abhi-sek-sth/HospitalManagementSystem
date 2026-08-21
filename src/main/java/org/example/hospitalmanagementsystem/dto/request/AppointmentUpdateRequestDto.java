package org.example.hospitalmanagementsystem.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentUpdateRequestDto(
        @NotNull(message = "Appointment date is required")
        @FutureOrPresent(message = "Appointment date cannot be in the past")
        LocalDate appointmentDate,

        @NotNull(message = "Appointment time is required")
        LocalTime appointmentTime,

        @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
        String reason,

        AppointmentStatus status,

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes
) {
}
