package dev.slethware.apigateway.controller;

import dev.slethware.apigateway.dto.response.ApiResponse;
import dev.slethware.apigateway.dto.response.HealthResponse;
import dev.slethware.apigateway.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthCheckService healthCheckService;

    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        HealthResponse health = healthCheckService.checkHealth();
        return ResponseEntity.ok(ApiResponse.success(health, "Health check complete"));
    }
}