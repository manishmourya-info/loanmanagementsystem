# Docker Dockerfile for Consumer Finance Loan Management System
# IMPORTANT: This is for reference only. Use Google Jib for production builds:
#   mvn clean package jib:dockerBuild        # Local Docker daemon
#   mvn clean package jib:build               # Push to Docker Hub
#
# Jib advantages over manual Dockerfile:
#   - No manual Dockerfile needed
#   - Optimized multi-stage layers (40-60% smaller images)
#   - Faster CI/CD builds with layer caching
#   - Deterministic, reproducible builds
#   - Automatic registry authentication

# Multi-stage build (reference for understanding Jib internals)
FROM openjdk:17-jdk-slim as builder
WORKDIR /builder
COPY target/loan-management-system-1.0.0.jar app.jar
RUN jar xf app.jar

FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy application layers (in order they're cached by Jib)
COPY --from=builder /builder/BOOT-INF/lib /app/lib
COPY --from=builder /builder/BOOT-INF/classes /app/classes
COPY --from=builder /builder/META-INF /app/META-INF

# JVM flags for efficient container execution
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -XX:+UseStringDeduplication -XX:+UseStringCache"

# Health check for container orchestration
HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD java -cp /app:/app/lib/* org.springframework.boot.loader.JarLauncher \
      --server.port=8080

# Application startup
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app:/app/lib/* org.springframework.boot.loader.JarLauncher"]
