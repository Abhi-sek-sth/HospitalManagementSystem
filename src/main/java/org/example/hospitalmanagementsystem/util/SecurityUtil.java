package org.example.hospitalmanagementsystem.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
@Component
public class SecurityUtil {

        public Authentication getAuthentication() {
            return SecurityContextHolder.getContext().getAuthentication();
        }

        public Long getCurrentUserId() {
            return Long.valueOf(getAuthentication().getName());
        }

        public boolean hasRole(String role) {
            return getAuthentication()
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals(role));
        }

        public boolean isAdmin() {
            return hasRole("ROLE_ADMIN");
        }

        public boolean isDoctor() {
            return hasRole("ROLE_DOCTOR");
        }

        public boolean isReceptionist() {
            return hasRole("ROLE_RECEPTIONIST");
        }

        public boolean isPatient() {
            return hasRole("ROLE_PATIENT");
        }
    }

