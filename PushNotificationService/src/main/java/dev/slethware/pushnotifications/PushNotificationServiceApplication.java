package dev.slethware.pushnotifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PushNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PushNotificationServiceApplication.class, args);
	}

}