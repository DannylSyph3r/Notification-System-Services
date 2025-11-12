package dev.slethware.pushnotifications.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.private-key}")
    private String privateKey;

    @Value("${firebase.client-email}")
    private String clientEmail;

    @Value("${firebase.service-account-file}")
    private String serviceAccountFile;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = buildFirebaseOptions();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully.");
        }
        return FirebaseApp.getInstance();
    }

    private FirebaseOptions buildFirebaseOptions() throws IOException {

        if (StringUtils.hasText(projectId) && StringUtils.hasText(privateKey) && StringUtils.hasText(clientEmail)) {
            log.info("Initializing Firebase using environment variables.");

            // Private key from env var sometimes comes with funny escaped newlines
            String formattedPrivateKey = privateKey.replace("\\n", "\n");

            String jsonConfig = String.format(
                    "{\"type\": \"service_account\", \"project_id\": \"%s\", \"private_key_id\": \"\", \"private_key\": \"%s\", \"client_email\": \"%s\", \"client_id\": \"\", \"auth_uri\": \"https...\", \"token_uri\": \"https...\", \"auth_provider_x509_cert_url\": \"https...\", \"client_x509_cert_url\": \"https...\"}",
                    projectId, formattedPrivateKey, clientEmail
            );

            try (InputStream serviceAccountStream = new ByteArrayInputStream(jsonConfig.getBytes(StandardCharsets.UTF_8))) {
                return FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();
            }
        }

        // Service Account File (for local development)
        if (StringUtils.hasText(serviceAccountFile) && serviceAccountFile.startsWith("classpath:")) {
            log.warn("Initializing Firebase using service account file from classpath. This is intended for local development.");
            try {
                InputStream serviceAccount = new ClassPathResource(serviceAccountFile.substring("classpath:".length())).getInputStream();
                return FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
            } catch (IOException e) {
                log.error("Failed to load Firebase service account file from classpath: {}", serviceAccountFile, e);
                throw new IOException("Failed to initialize Firebase from file", e);
            }
        }

        log.error("Firebase Admin SDK configuration is missing. " +
                "Set either FIREBASE_PROJECT_ID, FIREBASE_PRIVATE_KEY, and FIREBASE_CLIENT_EMAIL env vars " +
                "or define firebase.service-account-file in application.yml");
        throw new IllegalStateException("Could not configure Firebase Admin SDK");
    }


    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}