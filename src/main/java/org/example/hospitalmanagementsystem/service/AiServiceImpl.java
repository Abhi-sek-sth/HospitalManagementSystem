package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.AiRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AiResponseDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService{
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    @Override
    public AiResponseDto getMessage(AiRequestDto question) {

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question.question())
                        .topK(5)
                        .filterExpression("active == true")
                        .build()
        );
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String answer = chatClient.prompt()
                .system("""
                        You are a hospital information assistant.

                        Answer the user's question using ONLY the
                        hospital information provided in the context.

                        If the answer is not present in the context,
                        say that you do not have that information.

                        Do not invent hospital policies or information.

                        Context:
                        %s
                        """.formatted(context))
                .user(question.question())
                .call()
                .content();
        return new AiResponseDto(answer);
    }
    }

