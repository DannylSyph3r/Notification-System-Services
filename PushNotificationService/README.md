# Push Notification Service

## Overview

This service is part of a distributed notification system. Its sole responsibility is to consume push notification requests from a RabbitMQ queue, send them via Firebase Cloud Messaging (FCM), and report the status to Redis.

It also serves a simple web client for demonstrating web push registration and receiving test notifications.

### Features
- **RabbitMQ Consumer**: Listens to the `push.queue` for new notification jobs.
- **Firebase Integration**: Sends push notifications using the Firebase Admin SDK.
- **Retry Logic**: Implements exponential backoff for failed sends (up to 5 attempts).
- **Circuit Breaker**: Uses Resilience4j to protect against Firebase API downtime.
- **Status Tracking**: Reports notification status (`delivered`, `failed`, `skipped`) to Redis.
- **Dead Letter Queue**: Failed messages are routed to `failed.queue` after max retries.
- **Web Client Demo**: Serves a static `index.html` on `/` for testing web push.

## Technical Stack
- **Java 21**
- **Spring Boot 3.x**
- **RabbitMQ**: Message consumer
- **Redis**: Caching and status storage
- **Firebase Admin SDK**: For sending push notifications
- **Resilience4j**: Circuit breaker
- **Lombok**
- **Maven**

## Environment Variables

| Variable | Description | Example |
| --- | --- | --- |
| `SERVER_PORT` | Port for the service to run on. | `8082` |
| `SPRING_PROFILES_ACTIVE` | Spring profile (`dev`, `docker`, `prod`). | `docker` |
| `RABBITMQ_HOST` | RabbitMQ server hostname. | `rabbitmq` |
| `RABBITMQ_PORT` | RabbitMQ server port. | `5672` |
| `RABBITMQ_USER` | RabbitMQ username. | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password. | `guest` |
| `REDIS_HOST` | Redis server hostname. | `redis` |
| `REDIS_PORT` | Redis server port. | `6379` |
| `FIREBASE_SERVICE_ACCOUNT_FILE` | Path to Firebase JSON key (Option 1). | `classpath:firebase-service-account.json` |
| `FIREBASE_PROJECT_ID` | Firebase Project ID (Option 2). | `my-project-123` |
| `FIREBASE_PRIVATE_KEY` | Firebase Private Key (Option 2). | `"-----BEGIN PRIVATE KEY-----\n..."` |
| `FIREBASE_CLIENT_EMAIL` | Firebase Client Email (Option 2). | `firebase-adminsdk@...` |
| `WEB_CLIENT_URL` | Allowed origin for CORS (web client). | `http://localhost:8082` |


## Firebase Setup Instructions

Follow these steps to configure Firebase for this service.

### Step 1: Create Firebase Project
1.  Go to [https://console.firebase.google.com](https://console.firebase.google.com).
2.  Click **"Add project"**.
3.  Enter a project name (e.g., "NotificationSystemDemo").
4.  Disable Google Analytics for this project (optional, for quicker setup).
5.  Click **"Create project"**.

### Step 2: Get Service Account Key (for Backend)
This is required for the Java service to authenticate with Firebase.

1.  In your new project, click the **Gear icon** > **Project settings**.
2.  Go to the **"Service accounts"** tab.
3.  Click **"Generate new private key"**.
4.  A JSON file will be downloaded. Rename it to `firebase-service-account.json`.
5.  **For local development**: Place this file in `src/main/resources/`.
6.  **IMPORTANT**: Add `firebase-service-account.json` to your `.gitignore` file. **NEVER commit this file to Git.**
7.  **For production**: You will use the environment variables (`FIREBASE_PROJECT_ID`, `FIREBASE_PRIVATE_KEY`, `FIREBASE_CLIENT_EMAIL`) instead of this file. The `FirebaseConfig.java` is set up to prioritize env vars if they exist.

### Step 3: Get Web App Configuration (for Web Client)
This is required for the `app.js` in the static web client.

1.  In **Project settings**, go to the **"General"** tab.
2.  Scroll down to "Your apps". Click the **Web icon** (`</>`).
3.  Enter an app nickname (e.g., "Web Client Demo").
4.  Click **"Register app"**.
5.  You will see a `firebaseConfig` object. Copy this object.
6.  Paste this object into `src/main/resources/static/app.js`, replacing the placeholder.

### Step 4: Get VAPID Key (for Web Client)
This is required for web push notifications to work in browsers.

1.  In **Project settings**, go to the **"Cloud Messaging"** tab.
2.  Under **"Web configuration"**, find the **"Web Push certificates"** section.
3.  Click **"Generate key pair"**.
4.  A public key (VAPID key) will be generated.
5.  Copy this key.
6.  Paste this key into `src/main/resources/static/app.js`, replacing the `YOUR_VAPID_KEY` placeholder.

## Running Locally
1.  Ensure RabbitMQ and Redis are running (e.g., via Docker).
2.  Complete the Firebase Setup (Steps 2, 3, 4).
3.  Run the `PushNotificationServiceApplication` from your IDE.
4.  The service will be available at `http://localhost:8082`.

## Running with Docker
1.  Build the Docker image:
    ```sh
    docker build -t push-notification-service .
    ```
2.  Run with Docker, providing the necessary environment variables:
    ```sh
    docker run -p 8082:8082 \
      -e SPRING_PROFILES_ACTIVE=docker \
      -e RABBITMQ_HOST=your-rabbitmq-host \
      -e REDIS_HOST=your-redis-host \
      -e FIREBASE_PROJECT_ID=... \
      -e FIREBASE_PRIVATE_KEY=... \
      -e FIREBASE_CLIENT_EMAIL=... \
      push-notification-service
    ```
    (This service is typically run as part of the system's `docker-compose.yml`)

## Testing the Web Client
1.  Run the service.
2.  Open `http://localhost:8082` in your browser.
3.  Click **"Enable Notifications"** and accept the permission prompt.
4.  Your FCM Device Token will be displayed.
5.  Use this token in the API Gateway's `POST /api/v1/auth/register` or `PATCH /api/v1/users/me` endpoint to associate it with your user.
6.  Send a notification request to the API Gateway.
7.  If your browser tab is open (foreground), you will see the notification logged in the console and added to the "Received Notifications" list.
8.  If your browser tab is closed or in the background, you will receive a system notification.