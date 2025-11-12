package dev.slethware.pushnotifications.service;

import com.google.firebase.FirebaseApp;
import dev.slethware.pushnotifications.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final ConnectionFactory rabbitConnectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FirebaseApp firebaseApp; // Inject FirebaseApp to check initialization

    public HealthResponse checkHealth() {
        HealthResponse response = new HealthResponse(Instant.now().toString());

        response.addCheck("rabbitmq", checkRabbitMQ());
        response.addCheck("redis", checkRedis());
        response.addCheck("firebase", checkFirebase());

        // Determine overall status
        boolean allUp = response.getChecks().values().stream().allMatch(status -> status.equals(HealthResponse.STATUS_UP));
        response.setStatus(allUp ? HealthResponse.STATUS_UP : HealthResponse.STATUS_DOWN);

        return response;
    }

    private String checkRabbitMQ() {
        try {
            // The connection factory will throw an exception if it can't create a connection
            this.rabbitConnectionFactory.createConnection().close();
            return HealthResponse.STATUS_UP;
        } catch (Exception ex) {
            log.error("Health check failed for RabbitMQ: {}", ex.getMessage());
            return HealthResponse.STATUS_DOWN;
        }
    }

    private String checkRedis() {
        try {
            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                throw new IllegalStateException("RedisConnectionFactory is not available");
            }
            try (RedisConnection connection = factory.getConnection()) {
                connection.ping();
            }
            return HealthResponse.STATUS_UP;
        } catch (Exception ex) {
            log.error("Health check failed for Redis: {}", ex.getMessage());
            // As per requirement, gracefully handle if Redis is down
            return HealthResponse.STATUS_DOWN;
        }
    }

    private String checkFirebase() {
        try {
            // Check if the app is initialized and has a name
            if (firebaseApp != null && !firebaseApp.getName().isEmpty()) {
                return HealthResponse.STATUS_UP;
            } else {
                log.error("Health check failed for Firebase: App is null or has no name");
                return HealthResponse.STATUS_DOWN;
            }
        } catch (Exception ex) {
            log.error("Health check failed for Firebase: {}", ex.getMessage());
            return HealthResponse.STATUS_DOWN;
        }
    }
}