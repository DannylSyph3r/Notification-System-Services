package dev.slethware.apigateway.dto;

import lombok.Data;

@Data
public class UserPreferences {
    private boolean email = true;
    private boolean push = true;
}