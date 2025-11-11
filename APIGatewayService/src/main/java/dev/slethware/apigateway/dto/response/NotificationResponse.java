package dev.slethware.apigateway.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    @JsonProperty("notification_id")
    private String notificationId;

    private String status;
}