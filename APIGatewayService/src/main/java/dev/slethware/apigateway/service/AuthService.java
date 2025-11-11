package dev.slethware.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.slethware.apigateway.dto.request.LoginRequest;
import dev.slethware.apigateway.dto.request.RegisterRequest;
import dev.slethware.apigateway.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public LoginResponse register(RegisterRequest request) {
        log.info("Attempting to register new user: {}", request.getEmail());

        // 1. Forward request to User Service
        // The User Service creates the user and returns the JWT token in the response
        JsonNode response = userService.registerUser(request);

        // 2. Extract data from successful response
        // Expected JSON from User Service: { "data": { "token": "jwt...", "user_id": "uuid..." }, ... }
        JsonNode data = response.get("data");
        String userId = data.get("user_id").asText();
        String token = data.get("token").asText();

        log.info("Successfully registered user: {}", request.getEmail());
        return new LoginResponse(token, userId);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting to login user: {}", request.getEmail());

        // 1. Forward login request to User Service
        JsonNode response = userService.loginUser(request);

        // 2. Extract data from successful response
        // Expected JSON from User Service: { "data": { "token": "jwt...", "user_id": "uuid..." }, ... }
        JsonNode data = response.get("data");
        String userId = data.get("user_id").asText();
        String token = data.get("token").asText();

        log.info("Successfully logged in user: {}", request.getEmail());
        return new LoginResponse(token, userId);
    }
}