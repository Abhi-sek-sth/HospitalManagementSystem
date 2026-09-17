package org.example.hospitalmanagementsystem.dto.request;

import java.time.Duration;

public record RateLimitPolicy(
        int capacity,
        Duration refillPeriod
) {
}
