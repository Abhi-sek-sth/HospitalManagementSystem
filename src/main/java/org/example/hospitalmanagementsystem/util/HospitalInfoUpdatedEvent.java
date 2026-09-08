package org.example.hospitalmanagementsystem.util;

import org.example.hospitalmanagementsystem.entity.InformationCategory;

public record HospitalInfoUpdatedEvent(Long id,
                                       InformationCategory category,
                                       String title,
                                       String description
                                       ) {
}
