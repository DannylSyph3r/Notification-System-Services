package dev.slethware.pushnotifications.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponse {

    public static final String STATUS_UP = "UP";
    public static final String STATUS_DOWN = "DOWN";

    private String status;
    private String timestamp;
    private Map<String, String> checks;

    public HealthResponse(String timestamp) {
        this.timestamp = timestamp;
        this.checks = new HashMap<>();
    }

    public HealthResponse(String status, String timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

    public void addCheck(String name, String status) {
        this.checks.put(name, status);
    }
}