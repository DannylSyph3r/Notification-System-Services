package dev.slethware.pushnotifications.dto;

import lombok.Data;

@Data
public class UserPreferences {
    private boolean email = true;
    private boolean push = true;
}