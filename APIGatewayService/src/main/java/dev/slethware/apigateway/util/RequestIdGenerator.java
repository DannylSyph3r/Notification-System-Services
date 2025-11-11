package dev.slethware.apigateway.util;

import java.util.UUID;

public class RequestIdGenerator {

    private RequestIdGenerator() {}

    public static String generate() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        return "req-" + timestamp + "-" + uuid;
    }
}