package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.MedicalRecords;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalRecordsRepository extends JpaRepository<MedicalRecords,Long> {
    boolean existsByAppointmentId(Long appointmentId);

    Optional<MedicalRecords> findByAppointmentId(Long appointmentId);
}
