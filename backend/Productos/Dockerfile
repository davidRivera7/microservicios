FROM gradle:jdk17 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle clean build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar api-productos.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "api-productos.jar"]