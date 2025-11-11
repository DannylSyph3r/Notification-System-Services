package dev.slethware.apigateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    // Checks if a request ID has already been processed.
    public String getNotificationId(String requestId) {
        String key = IDEMPOTENCY_KEY_PREFIX + requestId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    // Stores the idempotency key in Redis to prevent duplicate processing.
    public void storeIdempotency(String requestId, String notificationId) {
        String key = IDEMPOTENCY_KEY_PREFIX + requestId;
        try {
            redisTemplate.opsForValue().set(key, notificationId, IDEMPOTENCY_TTL);
            log.info("Stored idempotency key: {}", key);
        } catch (Exception e) {
            log.error("Failed to store idempotency key: {}", key, e);
        }
    }
}