package dev.slethware.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.slethware.apigateway.dto.UserContact;
import dev.slethware.apigateway.dto.UserPreferences;
import dev.slethware.apigateway.dto.request.LoginRequest;
import dev.slethware.apigateway.dto.request.RegisterRequest;
import dev.slethware.apigateway.dto.request.UpdateUserRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String userServiceUrl;

    private static final String PREFERENCES_KEY_PREFIX = "user:preferences:";
    private static final String CONTACT_KEY_PREFIX = "user:contact:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private static final TypeReference<UserPreferences> PREFERENCES_TYPE_REFERENCE = new TypeReference<>() {};
    private static final TypeReference<UserContact> CONTACT_TYPE_REFERENCE = new TypeReference<>() {};


    public UserService(RestTemplate restTemplate,
                       RedisTemplate<String, Object> redisTemplate,
                       ObjectMapper redisObjectMapper,
                       @Value("${services.user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = redisObjectMapper;
        this.userServiceUrl = userServiceUrl;
    }


    public JsonNode registerUser(RegisterRequest request) {
        String url = userServiceUrl + "/api/v1/auth/register";
        return restTemplate.postForObject(url, request, JsonNode.class);
    }

    public JsonNode loginUser(LoginRequest request) {
        String url = userServiceUrl + "/api/v1/auth/login";
        return restTemplate.postForObject(url, request, JsonNode.class);
    }

    public JsonNode getUserProfile(UUID userId) {
        String url = userServiceUrl + "/internal/users/" + userId;
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public JsonNode updateUserProfile(UUID userId, UpdateUserRequest request) {
        String url = userServiceUrl + "/internal/users/" + userId;
        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, JsonNode.class);

        invalidateUserCaches(userId);
        return response.getBody();
    }


    public UserPreferences getUserPreferences(UUID userId, String correlationId) {
        String key = PREFERENCES_KEY_PREFIX + userId;

        // 1. Check cache
        try {
            // Get the value as a JSON String
            String cachedJson = (String) redisTemplate.opsForValue().get(key);
            if (cachedJson != null && !cachedJson.isEmpty()) {
                log.info("[{}] User preferences found in cache for user {}", correlationId, userId);
                // Convert from JSON String back to our DTO
                return objectMapper.readValue(cachedJson, PREFERENCES_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            log.warn("[{}] Failed to get/parse user preferences from cache: {}", correlationId, e.getMessage());
        }

        // 2. Fetch from service
        log.info("[{}] User preferences not in cache, fetching from User Service for user {}", correlationId, userId);
        String url = userServiceUrl + "/internal/users/" + userId + "/preferences";

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("data") || response.get("data").isNull()) {
                log.error("[{}] Malformed response from User Service (preferences): {}", correlationId, response);
                throw new RuntimeException("Malformed response from internal User Service");
            }

            UserPreferences preferences = objectMapper.convertValue(response.get("data"), UserPreferences.class);

            // 3. Store in cache
            if (preferences != null) {
                // Convert our DTO to a JSON String before saving
                String jsonToCache = objectMapper.writeValueAsString(preferences);
                redisTemplate.opsForValue().set(key, jsonToCache, CACHE_TTL);
            }
            return preferences;
        } catch (Exception e) {
            log.error("[{}] Failed to fetch user preferences from User Service: {}", correlationId, e.getMessage());
            throw new RuntimeException("Failed to fetch user preferences", e);
        }
    }

    public UserContact getUserContact(UUID userId, String correlationId) {
        String key = CONTACT_KEY_PREFIX + userId;

        // 1. Check cache
        try {
            // Get the value as a JSON String
            String cachedJson = (String) redisTemplate.opsForValue().get(key);
            if (cachedJson != null && !cachedJson.isEmpty()) {
                log.info("[{}] User contact found in cache for user {}", correlationId, userId);
                // Convert from JSON String back to our DTO
                return objectMapper.readValue(cachedJson, CONTACT_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            log.warn("[{}] Failed to get/parse user contact from cache: {}", correlationId, e.getMessage());
        }

        // 2. Fetch from service
        log.info("[{}] User contact not in cache, fetching from User Service for user {}", correlationId, userId);
        String url = userServiceUrl + "/internal/users/" + userId + "/contact";

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("data") || response.get("data").isNull()) {
                log.error("[{}] Malformed response from User Service (contact): {}", correlationId, response);
                throw new RuntimeException("Malformed response from internal User Service");
            }

            UserContact contact = objectMapper.convertValue(response.get("data"), UserContact.class);

            // 3. Store in cache
            if (contact != null) {
                // Convert our DTO to a JSON String before saving
                String jsonToCache = objectMapper.writeValueAsString(contact);
                redisTemplate.opsForValue().set(key, jsonToCache, CACHE_TTL);
            }
            return contact;
        } catch (Exception e) {
            log.error("[{}] Failed to fetch user contact from User Service: {}", correlationId, e.getMessage());
            throw new RuntimeException("Failed to fetch user contact", e);
        }
    }

    private void invalidateUserCaches(UUID userId) {
        try {
            redisTemplate.delete(PREFERENCES_KEY_PREFIX + userId);
            redisTemplate.delete(CONTACT_KEY_PREFIX + userId);
            log.info("Invalidated caches for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate caches for user {}: {}", userId, e.getMessage());
        }
    }
}