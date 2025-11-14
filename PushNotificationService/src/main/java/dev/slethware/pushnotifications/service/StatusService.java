package dev.slethware.pushnotifications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.slethware.pushnotifications.dto.StatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STATUS_KEY_PREFIX = "notification:status:";
    private static final Duration STATUS_TTL = Duration.ofHours(24);

    public void updateStatus(String notificationId, String status, String error) {
        String key = STATUS_KEY_PREFIX + notificationId;
        StatusUpdate statusUpdate = StatusUpdate.builder()
                .notificationId(notificationId)
                .status(status)
                .timestamp(Instant.now().toString())
                .error(error)
                .build();

        try {
            // Manually serialize to JSON string
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            String jsonString = mapper.writeValueAsString(statusUpdate);
            
            redisTemplate.opsForValue().set(key, jsonString, STATUS_TTL);
            log.info("Updated status for {}: {}", notificationId, status);
        } catch (Exception e) {
            // Gracefully handle Redis failure
            log.error("Failed to update status in Redis for {}: {}",
                    notificationId, e.getMessage(), e);
        }
    }

    public StatusUpdate getStatus(String notificationId) {
        String key = STATUS_KEY_PREFIX + notificationId;
        try {
            String jsonString = (String) redisTemplate.opsForValue().get(key);
            
            if (jsonString == null || jsonString.isEmpty()) {
                log.warn("Status not found for notification: {}", notificationId);
                return null;
            }
 
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            return mapper.readValue(jsonString, StatusUpdate.class);
            
        } catch (Exception e) {
            log.error("Failed to get status from Redis for {}: {}",
                    notificationId, e.getMessage(), e);
            return null;
        }
    }
}