package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.HospitalInfoRequestDto;
import org.example.hospitalmanagementsystem.entity.HospitalInfo;
import org.example.hospitalmanagementsystem.entity.InformationCategory;

public interface VectorIndexService {
    void indexDocument(Long id, InformationCategory category, String title, String description);
    void updateDocument(Long id, InformationCategory category, String title, String description);
    void deactivateDocument(Long id, InformationCategory category, String title, String description);
    void reactivateDocument(Long id, InformationCategory category, String title, String description);
    void deleteDocument(Long hospitalInfoId);
}
