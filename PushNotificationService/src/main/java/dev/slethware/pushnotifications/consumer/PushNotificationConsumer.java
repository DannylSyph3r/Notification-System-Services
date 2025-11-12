package dev.slethware.pushnotifications.consumer;

import com.rabbitmq.client.Channel;
import dev.slethware.pushnotifications.dto.NotificationMessage;
import dev.slethware.pushnotifications.exception.InvalidDeviceTokenException;
import dev.slethware.pushnotifications.service.PushNotificationService;
import dev.slethware.pushnotifications.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationConsumer {

    private final PushNotificationService pushNotificationService;
    private final StatusService statusService;

    @Value("${retry.max-attempts}")
    private int maxRetries;

    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    @RabbitListener(queues = "${rabbitmq.queue.push}", containerFactory = "rabbitListenerContainerFactory")
    public void consumePushNotification(
            @Payload NotificationMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(value = RETRY_COUNT_HEADER, defaultValue = "0") int retryCount) throws IOException {

        String correlationId = message.getCorrelationId();
        String notificationId = message.getNotificationId();

        try {
            log.info("[{}] Received push notification: {}. Attempt {}/{}",
                    correlationId, notificationId, retryCount + 1, maxRetries);

            pushNotificationService.sendPushNotification(message);

            // Success message
            channel.basicAck(deliveryTag, false);
            log.info("[{}] Successfully processed push notification {}", correlationId, notificationId);

        } catch (InvalidDeviceTokenException e) {
            // Invalid token, don't retry, acknowledge and update status to failed.
            log.warn("[{}] Invalid device token for {}. Rejecting message without retry.",
                    correlationId, notificationId, e);
            statusService.updateStatus(notificationId, "failed", "Invalid or unregistered device token");
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[{}] Error processing push notification {}: {}",
                    correlationId, notificationId, e.getMessage(), e);

            if (retryCount < (maxRetries - 1)) {
                // Retryable Failure
                log.warn("[{}] NACKing message {} for retry (attempt {}).",
                        correlationId, notificationId, retryCount + 1);
                statusService.updateStatus(notificationId, "pending",
                        String.format("Retry %d: %s", retryCount + 1, e.getMessage()));

                // Send to dead queue, route it back after its TTL elapses
                channel.basicNack(deliveryTag, false, false);
            } else {
                // Max Retries
                log.error("[{}] Max retries ({}) reached for {}. Moving to failed queue.",
                        correlationId, maxRetries, notificationId);
                statusService.updateStatus(notificationId, "failed",
                        String.format("Max retries reached: %s", e.getMessage()));

                channel.basicNack(deliveryTag, false, false);
            }
        }
    }
}