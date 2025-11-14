package dev.slethware.apigateway.controller;

import dev.slethware.apigateway.dto.request.NotificationRequest;
import dev.slethware.apigateway.dto.response.ApiResponse;
import dev.slethware.apigateway.dto.response.NotificationResponse;
import dev.slethware.apigateway.dto.response.StatusResponse;
import dev.slethware.apigateway.security.UserPrincipal;
import dev.slethware.apigateway.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> queueNotification(
            @Valid @RequestBody NotificationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
    
        request.setUserId(principal.id());
        
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response, "Notification queued successfully"));
    }

    @GetMapping("/{notification_id}/status")
    public ResponseEntity<ApiResponse<StatusResponse>> getNotificationStatus(
            @PathVariable("notification_id") String notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {

        StatusResponse response = notificationService.getNotificationStatus(notificationId);
        return ResponseEntity.ok(ApiResponse.success(response, "Status retrieved successfully"));
    }
}