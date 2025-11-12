package dev.slethware.pushnotifications.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PushPayload {
    private String deviceToken;
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;
}