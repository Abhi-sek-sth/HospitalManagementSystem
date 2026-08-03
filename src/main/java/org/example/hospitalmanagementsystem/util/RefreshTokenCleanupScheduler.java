package org.example.hospitalmanagementsystem.util;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional

    //Delete expire token on its own at 3 am everyday
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredBefore(Instant.now());
    }
}
