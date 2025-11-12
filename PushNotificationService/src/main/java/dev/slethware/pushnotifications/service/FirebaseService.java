package dev.slethware.pushnotifications.service;

import com.google.firebase.messaging.*;
import dev.slethware.pushnotifications.dto.PushPayload;
import dev.slethware.pushnotifications.exception.FirebaseException;
import dev.slethware.pushnotifications.exception.InvalidDeviceTokenException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final FirebaseMessaging firebaseMessaging;
    private static final String FIREBASE_CIRCUIT_BREAKER = "firebase";

    @CircuitBreaker(name = FIREBASE_CIRCUIT_BREAKER, fallbackMethod = "firebaseFallback")
    public String sendPushNotification(PushPayload payload, String correlationId) {
        try {
            Message message = buildFirebaseMessage(payload);
            String response = firebaseMessaging.send(message);
            log.info("[{}] Successfully sent message via Firebase: {}", correlationId, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("[{}] Firebase error: {} - Code: {}", correlationId, e.getMessage(), e.getMessagingErrorCode());
            handleFirebaseException(e);
            // This line is unlikely to be reached if handleFirebaseException throws,
            // but is here for completeness.
            throw new FirebaseException("Failed to send Firebase message: " + e.getMessage(), e);
        }
    }

    private Message buildFirebaseMessage(PushPayload payload) {
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(payload.getTitle())
                .setBody(payload.getBody());

        if (payload.getImageUrl() != null && !payload.getImageUrl().isBlank()) {
            notificationBuilder.setImage(payload.getImageUrl());
        }

        Message.Builder builder = Message.builder()
                .setToken(payload.getDeviceToken())
                .setNotification(notificationBuilder.build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .build());

        if (payload.getData() != null && !payload.getData().isEmpty()) {
            // Convert Map<String, Object> to Map<String, String>
            payload.getData().forEach((key, value) ->
                    builder.putData(key, String.valueOf(value))
            );
        }

        return builder.build();
    }

    private void handleFirebaseException(FirebaseMessagingException e) {
        MessagingErrorCode code = e.getMessagingErrorCode();
        if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
            // Token is invalid or expired. Do not retry.
            throw new InvalidDeviceTokenException("Firebase error: Invalid or unregistered device token. " + e.getMessage());
        }
        // For other errors (UNAVAILABLE, INTERNAL), throw a generic FirebaseException
        // which will trigger the consumer's retry logic.
        throw new FirebaseException("Firebase error: " + e.getMessage(), e);
    }

    @SuppressWarnings("unused")
    private String firebaseFallback(PushPayload payload, String correlationId, Throwable t) {
        log.error("[{}] Firebase circuit breaker is OPEN. Failing fast for token: {}",
                correlationId, payload.getDeviceToken(), t);
        throw new FirebaseException("Firebase service is unavailable. Circuit breaker is open.", t);
    }
}