package org.example.hospitalmanagementsystem.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GroqClientConfig {

    @Value("${spring.ai.openai.api-key}")
    private String groqApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String groqBaseUrl;

    @Value("${spring.ai.openai.chat.model}")
    private String groqModel;

    @Bean
    public OpenAiChatModel groqChatModel() {

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .apiKey(groqApiKey)
                .baseUrl(groqBaseUrl)
                .model(groqModel)
                .build();

        return OpenAiChatModel.builder()
                .options(options)
                .build();
    }
}