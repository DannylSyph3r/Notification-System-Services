FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ARG FIREBASE_CONFIG_JSON
ARG FIREBASE_VAPID_KEY

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /app/target/pushnotifications-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]