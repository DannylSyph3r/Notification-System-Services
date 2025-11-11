package dev.slethware.apigateway.exception;

import dev.slethware.apigateway.dto.response.NotificationResponse;

public class DuplicateRequestException extends RuntimeException {

    private final NotificationResponse cachedResponse;

    public DuplicateRequestException(String message, NotificationResponse cachedResponse) {
        super(message);
        this.cachedResponse = cachedResponse;
    }

    public NotificationResponse getCachedResponse() {
        return cachedResponse;
    }
}