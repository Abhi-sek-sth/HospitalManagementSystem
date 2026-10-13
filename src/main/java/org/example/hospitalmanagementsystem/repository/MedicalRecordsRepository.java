package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.MedicalRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MedicalRecordsRepository extends JpaRepository<MedicalRecords,Long> {
    boolean existsByAppointmentId(Long appointmentId);

    Optional<MedicalRecords> findByAppointmentId(Long appointmentId);

    @Query("""
    SELECT m
    FROM MedicalRecords m
    JOIN FETCH m.appointment a
    JOIN FETCH a.doctor
    WHERE m.id = :medicalRecordId
""")
    Optional<MedicalRecords> findByIdWithAppointmentAndDoctor(@Param("medicalRecordId") Long medicalRecordId);

}
