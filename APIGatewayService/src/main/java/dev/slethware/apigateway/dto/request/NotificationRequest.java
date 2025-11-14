package dev.slethware.apigateway.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class NotificationRequest {

    @NotNull(message = "Notification type is required")
    @JsonProperty("notification_type")
    private NotificationType notificationType;

    @JsonProperty("user_id")
    private UUID userId;

    @NotBlank(message = "Template code is required")
    @JsonProperty("template_code")
    private String templateCode;

    @NotNull(message = "Variables are required")
    private Map<String, Object> variables;

    @JsonProperty("request_id")
    private String requestId;

    private int priority = 1;

    private Map<String, Object> metadata = new HashMap<>();

    public enum NotificationType {
        EMAIL,
        PUSH
    }
}