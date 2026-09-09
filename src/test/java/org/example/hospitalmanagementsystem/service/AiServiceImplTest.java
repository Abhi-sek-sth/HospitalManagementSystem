package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.AiRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AiResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec promptSpec;

    @Mock
    private ChatClient.ChatClientRequestSpec systemSpec;

    @Mock
    private ChatClient.ChatClientRequestSpec userSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(vectorStore, chatClient);
    }
@Nested
@DisplayName("Search Active Document")
class SearchActiveDocument {
    @Test
    void getMessage_shouldSearchActiveDocumentsAndReturnAiResponse() {
        AiRequestDto request = new AiRequestDto("What are the visiting hours?");

        List<Document> documents = List.of(
                new Document("Visiting hours are from 9 AM to 6 PM."),
                new Document("Visitors must check in at reception.")
        );

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(documents);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(any(String.class))).thenReturn(systemSpec);
        when(systemSpec.user(request.question())).thenReturn(userSpec);
        when(userSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Visiting hours are from 9 AM to 6 PM.");

        AiResponseDto result = aiService.getMessage(request);

        assertThat(result).isNotNull();
        assertThat(result.answer()).isEqualTo("Visiting hours are from 9 AM to 6 PM.");

        ArgumentCaptor<SearchRequest> searchRequestCaptor = ArgumentCaptor.forClass(SearchRequest.class);

        verify(vectorStore).similaritySearch(searchRequestCaptor.capture());

        SearchRequest searchRequest = searchRequestCaptor.getValue();

        assertThat(searchRequest.getQuery()).isEqualTo("What are the visiting hours?");
        assertThat(searchRequest.getTopK()).isEqualTo(5);
        assertThat(searchRequest.getFilterExpression())
                .isNotNull();

        assertThat(searchRequest.getFilterExpression().toString())
                .contains("active")
                .contains("true");

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);

        verify(promptSpec).system(systemPromptCaptor.capture());

        String systemPrompt = systemPromptCaptor.getValue();

        assertThat(systemPrompt)
                .contains("Visiting hours are from 9 AM to 6 PM.")
                .contains("Visitors must check in at reception.")
                .contains("Answer the user's question using ONLY")
                .contains("Context:");

        verify(systemSpec).user(request.question());
        verify(userSpec).call();
        verify(responseSpec).content();
    }
}
    @Test
    void getMessage_shouldSupportEmptySearchResults() {
        AiRequestDto request = new AiRequestDto("What information is available?");

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(any(String.class))).thenReturn(systemSpec);
        when(systemSpec.user(request.question())).thenReturn(userSpec);
        when(userSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content())
                .thenReturn("I do not have that information.");

        AiResponseDto result = aiService.getMessage(request);

        assertThat(result.answer())
                .isEqualTo("I do not have that information.");

        ArgumentCaptor<String> systemPromptCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(promptSpec).system(systemPromptCaptor.capture());

        assertThat(systemPromptCaptor.getValue())
                .contains("Context:")
                .doesNotContain("null");

        verify(vectorStore).similaritySearch(any(SearchRequest.class));
        verify(responseSpec).content();
    }
}