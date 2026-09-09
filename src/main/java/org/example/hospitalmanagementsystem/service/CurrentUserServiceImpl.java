package org.example.hospitalmanagementsystem.service;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService{
    @Override
    public Long getCurrentUserId() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.valueOf(name);
        return userId;
    }

    @Override
    public String getCurrentRoles() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority();
}

    @Override
    public boolean isPatient() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_PATIENT".equals(authority.getAuthority()));
        }




    @Override
    public boolean isReceptionist() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_RECEPTIONIST".equals(authority.getAuthority()));
    }

    @Override
    public boolean isAdmin() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    @Override
    public boolean isDoctor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_DOCTOR".equals(authority.getAuthority()));    }
}
