package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.entity.InformationCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VectorIndexServiceImplTest {

    @Mock
    private VectorStore vectorStore;

    private VectorIndexServiceImpl vectorIndexService;

    @BeforeEach
    void setUp() {
        vectorIndexService = new VectorIndexServiceImpl(vectorStore);
    }
@Nested
@DisplayName("Add new Info")
class NewInfo {
    @Test
    void indexDocument_shouldDeleteExistingDocumentAndAddActiveDocument() {
        Long id = 10L;
        InformationCategory category = InformationCategory.GENERAL;
        String title = "Emergency Department";
        String description = "The emergency department is open 24 hours.";

        vectorIndexService.indexDocument(id, category, title, description);

        String expectedVectorId = expectedVectorId(id);

        verify(vectorStore).delete(List.of(expectedVectorId));

        ArgumentCaptor<List<Document>> documentsCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(vectorStore).add(documentsCaptor.capture());

        Document document = documentsCaptor.getValue().get(0);

        assertThat(document.getId())
                .isEqualTo(expectedVectorId);

        assertThat(document.getText())
                .contains("Category: " + category)
                .contains("Title: " + title)
                .contains("Description: " + description);

        assertThat(document.getMetadata())
                .containsEntry("docId", id.toString())
                .containsEntry("category", category.name())
                .containsEntry("title", title)
                .containsEntry("active", true);
    }
}
@Nested
@DisplayName("UpdateInfo")
class Update {
    @Test
    void updateDocument_shouldUpsertActiveDocument() {
        Long id = 20L;

        vectorIndexService.updateDocument(
                id,
                InformationCategory.GENERAL,
                "Pharmacy",
                "The pharmacy is located on the first floor."
        );

        String expectedVectorId = expectedVectorId(id);

        verify(vectorStore).delete(List.of(expectedVectorId));
        verify(vectorStore).add(anyList());
    }
}
@Nested
@DisplayName("DeacitvateDocument")
class Deactivate {
    @Test
    void deactivateDocument_shouldStoreDocumentAsInactive() {
        Long id = 30L;
        InformationCategory category = InformationCategory.GENERAL;

        vectorIndexService.deactivateDocument(
                id,
                category,
                "Outpatient Clinic",
                "The outpatient clinic operates from 8 AM to 5 PM."
        );

        ArgumentCaptor<List<Document>> documentsCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(vectorStore).add(documentsCaptor.capture());

        Document document = documentsCaptor.getValue().get(0);

        assertThat(document.getMetadata())
                .containsEntry("active", false);
    }
}
@Nested
@DisplayName("ReactivateInfo")
class Reactivate {
    @Test
    void reactivateDocument_shouldStoreDocumentAsActive() {
        Long id = 40L;

        vectorIndexService.reactivateDocument(
                id,
                InformationCategory.GENERAL,
                "Laboratory",
                "Laboratory services are available on weekdays."
        );

        ArgumentCaptor<List<Document>> documentsCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(vectorStore).add(documentsCaptor.capture());

        Document document = documentsCaptor.getValue().get(0);

        assertThat(document.getMetadata())
                .containsEntry("active", true);
    }
}
    @Test
    void upsertDocument_shouldIgnoreNullDescription() {
        vectorIndexService.indexDocument(
                50L,
                InformationCategory.GENERAL,
                "Test title",
                null
        );

        verifyNoInteractions(vectorStore);
    }

    @Test
    void upsertDocument_shouldIgnoreBlankDescription() {
        vectorIndexService.indexDocument(
                60L,
                InformationCategory.GENERAL,
                "Test title",
                "   "
        );

        verifyNoInteractions(vectorStore);
    }

    @Test
    void deleteDocument_shouldDeleteDocumentUsingDeterministicVectorId() {
        Long hospitalInfoId = 70L;

        vectorIndexService.deleteDocument(hospitalInfoId);

        String expectedVectorId = expectedVectorId(hospitalInfoId);

        verify(vectorStore).delete(List.of(expectedVectorId));
        verifyNoMoreInteractions(vectorStore);
    }

    private String expectedVectorId(Long hospitalInfoId) {
        return UUID.nameUUIDFromBytes(
                ("hospital-info-" + hospitalInfoId)
                        .getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}


