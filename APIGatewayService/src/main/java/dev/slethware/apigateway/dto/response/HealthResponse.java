package dev.slethware.apigateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {

    private String status;
    private Map<String, String> services;
    private String timestamp;
}