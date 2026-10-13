package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    // @SQLRestriction on the entity automatically excludes soft-deleted items here.
    boolean existsByPrescriptionIdAndMedicationId(Long prescriptionId, Long medicationId);

    @Query("""
            select i from PrescriptionItem i
            join fetch i.medication
            where i.prescription.id = :prescriptionId
            order by i.id
            """)
    List<PrescriptionItem> findAllByPrescriptionIdWithMedication(@Param("prescriptionId") Long prescriptionId);

    /** Loads the item with the full authorization graph in ONE query (no N+1). */
    @Query("""
            select i from PrescriptionItem i
            join fetch i.medication
            join fetch i.prescription p
            join fetch p.medicalRecord mr
            join fetch mr.appointment a
            join fetch a.doctor d
            join fetch d.user
            join fetch a.patient pt
            join fetch pt.user
            where i.id = :id and p.deleted = false
            """)
    Optional<PrescriptionItem> findByIdWithContext(@Param("id") Long id);
}