package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );

    Page<Appointment> findByPatientId(
            Long patientId,
            Pageable pageable
    );

    Page<Appointment> findByDoctorId(
            Long doctorId,
            Pageable pageable
    );

    Page<Appointment> findByStatus(
            AppointmentStatus status,
            Pageable pageable
    );
}
