package dev.slethware.apigateway.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.slethware.apigateway.dto.UserContact;
import dev.slethware.apigateway.dto.UserPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    @JsonProperty("notification_id")
    private String notificationId;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("notification_type")
    private String notificationType; // "EMAIL" or "PUSH"

    @JsonProperty("template_code")
    private String templateCode;

    private Map<String, Object> variables;

    private int priority;

    private Map<String, Object> metadata;

    @JsonProperty("user_preferences")
    private UserPreferences userPreferences;

    @JsonProperty("user_contact")
    private UserContact userContact;

    @JsonProperty("correlation_id")
    private String correlationId;
}