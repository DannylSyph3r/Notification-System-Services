package dev.slethware.pushnotifications.controller;

import dev.slethware.pushnotifications.dto.HealthResponse;
import dev.slethware.pushnotifications.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthCheckService healthCheckService;

    @GetMapping
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse health = healthCheckService.checkHealth();
        HttpStatus status = health.getStatus().equals(HealthResponse.STATUS_UP) ?
                HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }

    @GetMapping("/live")
    public ResponseEntity<HealthResponse> getLiveness() {
        // Means the app is running
        HealthResponse response = new HealthResponse(HealthResponse.STATUS_UP, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<HealthResponse> getReadiness() {
        // Means the app and its dependencies are buzzing to go
        HealthResponse health = healthCheckService.checkHealth();
        HttpStatus status = health.getStatus().equals(HealthResponse.STATUS_UP) ?
                HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }
}