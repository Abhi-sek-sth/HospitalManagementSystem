package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hospitalmanagementsystem.entity.InformationCategory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorIndexServiceImpl implements VectorIndexService {

    private final VectorStore vectorStore;

    @Override
    public void indexDocument(Long id, InformationCategory category, String title, String description) {
        upsertDocument(id, category, title, description, true);
    }

    @Override
    public void updateDocument(Long id, InformationCategory category, String title, String description) {
        upsertDocument(id, category, title, description, true);
    }

    @Override
    public void deactivateDocument(Long id, InformationCategory category, String title, String description) {
        upsertDocument(id, category, title, description, false);
    }

    @Override
    public void reactivateDocument(Long id, InformationCategory category, String title, String description) {
        upsertDocument(id, category, title, description, true);
    }

    @Override
    public void deleteDocument(Long hospitalInfoId) {
        String vectorId = toVectorId(hospitalInfoId);
        log.info("Hard-deleting vector document hospitalInfoId={} vectorId={}", hospitalInfoId, vectorId);
        vectorStore.delete(List.of(vectorId));
    }

    private void upsertDocument(Long id, InformationCategory category, String title,
                                String description, boolean active) {
        if (description == null || description.isBlank()) {
            return;
        }

        String content = """
            Category: %s
            Title: %s
            Description: %s
            """.formatted(category, title, description);

        String vectorId = toVectorId(id);

        Document document = new Document(
                vectorId,
                content,
                Map.of(
                        "docId", id.toString(),
                        "category", category.name(),
                        "title", title,
                        "active", active
                )
        );

        vectorStore.delete(List.of(vectorId));
        vectorStore.add(List.of(document));

        log.info("Upserted vector doc hospitalInfoId={} vectorId={} active={}", id, vectorId, active);
    }


    private String toVectorId(Long hospitalInfoId) {
        return UUID.nameUUIDFromBytes(
                ("hospital-info-" + hospitalInfoId).getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}