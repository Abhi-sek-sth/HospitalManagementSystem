package org.example.hospitalmanagementsystem.config;

import org.example.hospitalmanagementsystem.dto.request.RateLimitPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RateLimitRulesConfig {

    @Bean
    public Map<String, RateLimitPolicy> rateLimitsConfig() {
        return Map.of(
                "/login", new RateLimitPolicy(4, Duration.ofMinutes(1)),

                "/register", new RateLimitPolicy(3, Duration.ofMinutes(1)),

                "/api/appointments/*/recommendation", new RateLimitPolicy(3, Duration.ofMinutes(1))
        );
    }
}