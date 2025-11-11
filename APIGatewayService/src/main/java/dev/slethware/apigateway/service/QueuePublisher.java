package dev.slethware.apigateway.service;

import dev.slethware.apigateway.queue.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueuePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing-keys.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing-keys.push}")
    private String pushRoutingKey;

    public void publishNotification(NotificationMessage message) {
        String routingKey = switch (message.getNotificationType().toUpperCase()) {
            case "EMAIL" -> emailRoutingKey;
            case "PUSH" -> pushRoutingKey;
            default -> {
                log.error("[{}] Unknown notification type: {}", message.getCorrelationId(), message.getNotificationType());
                throw new IllegalArgumentException("Unknown notification type");
            }
        };

        log.info("[{}] Publishing notification {} to exchange {} with routing key {}",
                message.getCorrelationId(), message.getNotificationId(), exchangeName, routingKey);

        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message, postProcessor -> {
                postProcessor.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                postProcessor.getMessageProperties().setCorrelationId(message.getCorrelationId());
                postProcessor.getMessageProperties().setHeader("request_id", message.getRequestId());
                return postProcessor;
            });
        } catch (Exception e) {
            log.error("[{}] Failed to publish message to RabbitMQ: {}", message.getCorrelationId(), e.getMessage(), e);
            throw new RuntimeException("Failed to queue notification", e);
        }
    }
}