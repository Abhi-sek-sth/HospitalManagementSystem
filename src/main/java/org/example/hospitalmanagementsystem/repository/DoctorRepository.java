package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
Boolean existsByLicenseNumber(String licenseNumber);
Boolean existsByUserId(Long id);
}
