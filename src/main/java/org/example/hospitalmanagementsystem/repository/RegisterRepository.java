package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.Register;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterRepository extends JpaRepository<Register,Long> {
    Optional<Register> findByEmail(String email);

    Optional<Register> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}
