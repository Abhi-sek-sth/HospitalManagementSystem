package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles,Long> {

    Optional<Roles> findByRoleName(String role);
}
