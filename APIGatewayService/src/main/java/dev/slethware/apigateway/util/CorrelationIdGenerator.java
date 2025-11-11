package dev.slethware.apigateway.util;

import java.util.UUID;

public class CorrelationIdGenerator {

    private CorrelationIdGenerator() {}

    public static String generate() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        return "corr-" + timestamp + "-" + shortUuid;
    }
}