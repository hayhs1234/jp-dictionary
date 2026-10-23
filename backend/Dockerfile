# ==============================================================================
# Multi-stage Dockerfile for Spring Boot 3 Java 21 Backend
# ==============================================================================

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache Maven dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Security: Upgrade Alpine OS packages to patch any base CVEs, then run as non-root user
RUN apk update && apk upgrade --no-cache && \
    addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy jar artifact from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
