FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ARG FIREBASE_CONFIG_JSON
ARG FIREBASE_VAPID_KEY

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN cp src/main/resources/static/app-example.js src/main/resources/static/app.js && \
    cp src/main/resources/static/firebase-messaging-sw-example.js src/main/resources/static/firebase-messaging-sw.js

RUN sed -i "s|__FIREBASE_CONFIG_JSON_PLACEHOLDER__|${FIREBASE_CONFIG_JSON}|g" src/main/resources/static/app.js && \
    sed -i "s|__FIREBASE_VAPID_KEY_PLACEHOLDER__|${FIREBASE_VAPID_KEY}|g" src/main/resources/static/app.js && \
    sed -i "s|__FIREBASE_CONFIG_JSON_PLACEHOLDER__|${FIREBASE_CONFIG_JSON}|g" src/main/resources/static/firebase-messaging-sw.js

RUN ./mvnw clean package -DskipTests

# Create the final production image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/pushnotifications-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]