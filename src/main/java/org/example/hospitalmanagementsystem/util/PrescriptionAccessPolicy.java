package org.example.hospitalmanagementsystem.util;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.exception.AppointmentNotCompletedException;
import org.example.hospitalmanagementsystem.exception.ForbiddenOperationException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;


@Component
@RequiredArgsConstructor
public class PrescriptionAccessPolicy {

    private final SecurityUtil securityUtil;

    public void requireDoctorRole(String operation) {
        if (!securityUtil.isDoctor()) {
            throw new ForbiddenOperationException("Only doctors can " + operation + " prescriptions");
        }
    }

    public void requireOwningDoctor(Appointment appointment, Long userId, Supplier<String> message) {
        if (!isOwningDoctor(appointment, userId)) {
            throw new ForbiddenOperationException(message.get());
        }
    }

    public void requireParticipant(Appointment appointment, Long userId) {
        if (!isOwningDoctor(appointment, userId) && !isOwningPatient(appointment, userId)) {
            throw new ForbiddenOperationException("You are not allowed to access this appointment" );
        }
    }

    public void requireCompleted(Appointment appointment) {
        if (appointment == null || !AppointmentStatus.COMPLETED.equals(appointment.getStatus())) {
            throw new AppointmentNotCompletedException("Your appointment has not been completed yet");
        }
    }

    public void requireActiveDoctor(Doctor doctor, String message) {
        if (!Boolean.TRUE.equals(doctor.getActive())) {
            throw new ForbiddenOperationException(message);
        }
    }

    private static boolean isOwningDoctor(Appointment appointment, Long userId) {
        return userId.equals(appointment.getDoctor().getUser().getId());
    }

    private static boolean isOwningPatient(Appointment appointment, Long userId) {
        return userId.equals(appointment.getPatient().getUser().getId());
    }
}