package dev.slethware.apigateway.service;

import dev.slethware.apigateway.dto.response.HealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConnectionFactory rabbitConnectionFactory;

    public HealthResponse checkHealth() {
        Map<String, String> services = new HashMap<>();

        // Check Redis
        services.put("redis", checkRedis());

        // Check RabbitMQ
        services.put("rabbitmq", checkRabbitMQ());

        // Determine overall status
        String status = services.values().stream()
                .allMatch(s -> s.equals("connected")) ? "healthy" : "degraded";

        return new HealthResponse(status, services, Instant.now().toString());
    }

    private String checkRedis() {
        try {
            // Get the connection factory from the template
            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                throw new IllegalStateException("RedisConnectionFactory is not available");
            }
            // Get and close a connection to test
            try (RedisConnection connection = factory.getConnection()) {
                connection.ping(); // Execute the ping command
            }
            return "connected";
        } catch (Exception ex) {
            log.error("Redis health check failed: {}", ex.getMessage());
            return "disconnected";
        }
    }

    private String checkRabbitMQ() {
        try {
            // The connection factory will throw an exception if it can't create a connection
            this.rabbitConnectionFactory.createConnection().close();
            return "connected";
        } catch (Exception ex) {
            log.error("RabbitMQ health check failed: {}", ex.getMessage());
            return "disconnected";
        }
    }
}