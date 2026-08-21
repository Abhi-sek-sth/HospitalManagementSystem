package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.entity.Roles;

public interface CurrentUserService {
    Long getCurrentUserId();
    String getCurrentRoles();
    boolean isPatient();
    boolean isReceptionist();
    boolean isAdmin();
    boolean isDoctor();
}
