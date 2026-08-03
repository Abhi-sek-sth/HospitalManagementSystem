package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    Optional<Department> findByName(String name);
}
