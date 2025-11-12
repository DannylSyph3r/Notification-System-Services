package dev.slethware.pushnotifications.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RetryUtil {

    private final long baseDelay;
    private final long maxDelay;

    public RetryUtil(
            @Value("${retry.base-delay-ms}") long baseDelay,
            @Value("${retry.max-delay-ms}") long maxDelay) {
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    // Calculates exponential backoff delay.
    public long calculateBackoff(int retryCount) {
        long delay = baseDelay * (long) Math.pow(2, retryCount);
        return Math.min(delay, maxDelay);
    }
}