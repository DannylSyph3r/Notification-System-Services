package dev.slethware.apigateway.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.slethware.apigateway.dto.UserPreferences;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    @JsonProperty("push_token")
    private String pushToken;

    private UserPreferences preferences;
}