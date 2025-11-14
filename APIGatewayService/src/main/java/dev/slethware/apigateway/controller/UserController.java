package dev.slethware.apigateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import dev.slethware.apigateway.dto.request.UpdateUserRequest;
import dev.slethware.apigateway.dto.response.ApiResponse;
import dev.slethware.apigateway.security.UserPrincipal;
import dev.slethware.apigateway.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<JsonNode>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.id();
        JsonNode userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(userProfile, "User profile retrieved successfully"));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<JsonNode>> updateMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid @RequestBody UpdateUserRequest request) {
        UUID userId = principal.id();
        JsonNode updatedUser = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User profile updated successfully"));
    }
}