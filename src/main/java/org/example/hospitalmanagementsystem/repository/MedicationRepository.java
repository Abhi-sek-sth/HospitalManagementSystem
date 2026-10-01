package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    boolean existsByNameIgnoreCaseAndManufacturerIgnoreCase(String name, String manufacturer);

    Page<Medication> findByActiveTrue(Pageable pageable);

    Page<Medication> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
}
