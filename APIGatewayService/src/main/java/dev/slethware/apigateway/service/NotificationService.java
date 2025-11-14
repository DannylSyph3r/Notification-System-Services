package dev.slethware.apigateway.service;

import dev.slethware.apigateway.dto.UserContact;
import dev.slethware.apigateway.dto.UserPreferences;
import dev.slethware.apigateway.dto.request.NotificationRequest;
import dev.slethware.apigateway.dto.response.NotificationResponse;
import dev.slethware.apigateway.dto.response.StatusResponse;
import dev.slethware.apigateway.exception.BadRequestException;
import dev.slethware.apigateway.exception.DuplicateRequestException;
import dev.slethware.apigateway.exception.ResourceNotFoundException;
import dev.slethware.apigateway.queue.NotificationMessage;
import dev.slethware.apigateway.util.CorrelationIdGenerator;
import dev.slethware.apigateway.util.RequestIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final IdempotencyService idempotencyService;
    private final UserService userService;
    private final QueuePublisher queuePublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STATUS_KEY_PREFIX = "notification:status:";
    private static final Duration STATUS_TTL = Duration.ofHours(24);

    public NotificationResponse sendNotification(NotificationRequest request) {

        String correlationId = CorrelationIdGenerator.generate();
        log.info("[{}] Notification request received: type={}, user={}",
                correlationId, request.getNotificationType(), request.getUserId());

        // 1. Generate request_id if not provided
        String requestId = request.getRequestId();
        if (requestId == null || requestId.isBlank()) {
            requestId = RequestIdGenerator.generate();
            log.warn("[{}] No request_id provided by client, generating new one: {}. Idempotency is not guaranteed.",
                    correlationId, requestId);
        }

        // 2. Check idempotency
        String existingNotificationId = idempotencyService.getNotificationId(requestId);
        if (existingNotificationId != null) {
            log.warn("[{}] Duplicate request detected (idempotency key: {}). Returning cached response.",
                    correlationId, requestId);
            StatusResponse cachedStatus = getNotificationStatus(existingNotificationId);
            NotificationResponse cachedResponse = new NotificationResponse(cachedStatus.getNotificationId(), cachedStatus.getStatus());
            throw new DuplicateRequestException("Duplicate request: Notification already processed", cachedResponse);
        }

        // 3. Generate new notification_id
        String notificationId = UUID.randomUUID().toString();

        // 4. Fetch user preferences
        UserPreferences preferences = userService.getUserPreferences(request.getUserId(), correlationId);

        // 5. Check if user has enabled this notification type
        if (request.getNotificationType() == NotificationRequest.NotificationType.EMAIL && !preferences.isEmail()) {
            throw new BadRequestException("User has disabled email notifications");
        }
        if (request.getNotificationType() == NotificationRequest.NotificationType.PUSH && !preferences.isPush()) {
            throw new BadRequestException("User has disabled push notifications");
        }

        // 6. Fetch user contact info
        UserContact contact = userService.getUserContact(request.getUserId(), correlationId);

        // 7. Build NotificationMessage for queue
        NotificationMessage message = NotificationMessage.builder()
                .notificationId(notificationId)
                .requestId(requestId)
                .userId(request.getUserId().toString())
                .notificationType(request.getNotificationType().name())
                .templateCode(request.getTemplateCode())
                .variables(request.getVariables())
                .priority(request.getPriority())
                .metadata(request.getMetadata())
                .userPreferences(preferences)
                .userContact(contact)
                .correlationId(correlationId)
                .build();

        // 8. Publish to queue
        queuePublisher.publishNotification(message);

        // 9. Store initial status in Redis
        storeStatus(notificationId, null);

        // 10. Store idempotency key
        idempotencyService.storeIdempotency(requestId, notificationId);

        // 11. Return response
        log.info("[{}] Notification queued successfully: {}", correlationId, notificationId);
        return new NotificationResponse(notificationId, "pending");
    }

    public StatusResponse getNotificationStatus(String notificationId) {
        String key = STATUS_KEY_PREFIX + notificationId;
        
        try {
            // Get raw string from Redis
            String jsonString = (String) redisTemplate.opsForValue().get(key);
            
            if (jsonString == null || jsonString.isEmpty()) {
                log.warn("Notification status not found for ID: {}", notificationId);
                throw new ResourceNotFoundException("Notification status not found");
            }
            
            // Manually deserialize using ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            StatusResponse status = mapper.readValue(jsonString, StatusResponse.class);
            
            return status;
        } catch (Exception e) {
            log.error("Failed to deserialize status from Redis: {}", e.getMessage());
            throw new ResourceNotFoundException("Notification status not found");
        }
    }
    
    private void storeStatus(String notificationId, String error) {
        String key = STATUS_KEY_PREFIX + notificationId;
        StatusResponse statusResponse = new StatusResponse(
                notificationId,
                "pending",
                Instant.now().toString(),
                error
        );
        try {
            // Manually serialize to JSON string
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            String jsonString = mapper.writeValueAsString(statusResponse);
            
            redisTemplate.opsForValue().set(key, jsonString, STATUS_TTL);
        } catch (Exception e) {
            log.error("Failed to store notification status in Redis: {}", e.getMessage());
        }
    }
}