package dev.slethware.apigateway.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.slethware.apigateway.dto.UserPreferences;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @JsonProperty("push_token")
    private String pushToken;

    @NotNull(message = "Preferences are required")
    private UserPreferences preferences;
}