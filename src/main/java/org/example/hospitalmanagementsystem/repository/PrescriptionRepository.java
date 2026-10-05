package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription,Long> {

    List<Prescription> findByMedicalRecordIdOrderByPrescribedAtDesc(Long medicalRecordId);

    boolean existsByMedicalRecordId(Long medicalRecordId);

    @Query("""
        SELECT p
        FROM Prescription p
        JOIN FETCH p.medicalRecord m
        JOIN FETCH m.appointment a
        JOIN FETCH a.doctor
        JOIN FETCH a.patient
        WHERE p.id = :prescriptionId
    """)
    Optional<Prescription> findByIdWithRecordAppointmentAndParticipants(
            @Param("prescriptionId") Long prescriptionId);

    Page<Prescription> findAllByMedicalRecordId(Long medicalRecordId, Pageable pageable);
}
