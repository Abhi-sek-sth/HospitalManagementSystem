package org.example.hospitalmanagementsystem.util;

import org.example.hospitalmanagementsystem.entity.InformationCategory;

public record HospitalInfoCreatedEvent(
        Long id,
        InformationCategory category,
        String title,
        String description ) {
}
