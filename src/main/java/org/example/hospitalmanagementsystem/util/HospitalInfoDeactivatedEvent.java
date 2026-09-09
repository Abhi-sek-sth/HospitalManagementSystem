package org.example.hospitalmanagementsystem.util;

import org.example.hospitalmanagementsystem.entity.InformationCategory;

public record HospitalInfoDeactivatedEvent(Long hospitalInfoId,
                                           InformationCategory category,
                                           String title,
                                           String description) {
}
