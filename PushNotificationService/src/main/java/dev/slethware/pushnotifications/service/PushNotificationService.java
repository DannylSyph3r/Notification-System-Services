package dev.slethware.pushnotifications.service;

import dev.slethware.pushnotifications.dto.NotificationMessage;
import dev.slethware.pushnotifications.dto.PushPayload;
import dev.slethware.pushnotifications.dto.UserContact;
import dev.slethware.pushnotifications.dto.UserPreferences;
import dev.slethware.pushnotifications.exception.InvalidDeviceTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseService firebaseService;
    private final StatusService statusService;

    public void sendPushNotification(NotificationMessage message) {
        String correlationId = message.getCorrelationId();
        String notificationId = message.getNotificationId();

        // 1. Validate Device Token
        String deviceToken = Optional.ofNullable(message.getUserContact())
                .map(UserContact::getPushToken)
                .filter(token -> !token.isBlank())
                .orElseThrow(() -> new InvalidDeviceTokenException("Device token is null or empty"));

        // 2. Check User Preferences
        boolean pushEnabled = Optional.ofNullable(message.getUserPreferences())
                .map(UserPreferences::isPush)
                .orElse(true); // Default to true if preferences are missing

        if (!pushEnabled) {
            log.warn("[{}] User has disabled push notifications. Skipping notification {}",
                    correlationId, notificationId);
            statusService.updateStatus(notificationId, "skipped", "User has disabled push notifications");
            return;
        }

        // 3. Build Push Payload (No template service call needed for push)
        Map<String, Object> variables = message.getVariables();
        String title = (String) variables.getOrDefault("title", "New Notification");
        String body = (String) variables.getOrDefault("body", "You have a new update.");
        String imageUrl = (String) variables.get("imageUrl"); // Can be null

        PushPayload payload = PushPayload.builder()
                .deviceToken(deviceToken)
                .title(title)
                .body(body)
                .imageUrl(imageUrl)
                .build();

        // 4. Send via Firebase
        try {
            statusService.updateStatus(notificationId, "pending", null);
            String messageId = firebaseService.sendPushNotification(payload, correlationId);
            log.info("[{}] Push notification sent successfully via Firebase, messageId: {}",
                    correlationId, messageId);

            // 5. Update Status to Delivered
            statusService.updateStatus(notificationId, "delivered", null);

        } catch (Exception e) {
            // Log error and update status
            log.error("[{}] Failed to send push notification {}: {}",
                    correlationId, notificationId, e.getMessage(), e);
            statusService.updateStatus(notificationId, "failed", e.getMessage());
            // Re-throw to trigger retry logic in consumer
            throw new RuntimeException("Failed to send push notification: " + e.getMessage(), e);
        }
    }
}