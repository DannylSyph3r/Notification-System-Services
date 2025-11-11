package dev.slethware.apigateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponse {

    @JsonProperty("notification_id")
    private String notificationId;

    private String status; // "delivered", "pending", "failed"
    private String timestamp;
    private String error;
}