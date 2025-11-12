package dev.slethware.pushnotifications.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class WebClientController {

    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping(value = "/firebase-messaging-sw.js", produces = "application/javascript")
    public ResponseEntity<String> serviceWorker() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/firebase-messaging-sw.js");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/javascript"))
                .header("Service-Worker-Allowed", "/")
                .body(content);
    }
}