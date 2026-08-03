package org.example.hospitalmanagementsystem.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenCache {

    private static final String PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;

    public void store(String tokenHash, Long userId, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + tokenHash, String.valueOf(userId), ttl);
    }



    public void invalidate(String tokenHash) {
        redisTemplate.delete(PREFIX + tokenHash);
    }

    public void invalidateAll(Iterable<String> tokenHashes) {
        for (String hash : tokenHashes) {
            invalidate(hash);
        }
    }
}
