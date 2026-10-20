# ==========================================
# Etapa 1: Compilacion y empaquetado (Build)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de configuracion de Gradle primero para aprovechar la caché de capas
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Otorgar permisos de ejecucion al wrapper de Gradle
RUN chmod +x gradlew

# Copiar el codigo fuente de la aplicacion
COPY src src

# Compilar la aplicacion omitiendo los tests para agilizar el proceso
RUN ./gradlew bootJar -x test

# ==========================================
# Etapa 2: Imagen de ejecucion final (Runtime)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar únicamente el archivo JAR generado desde la etapa de compilación
COPY --from=builder /app/build/libs/*.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8084

# Comando para iniciar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]