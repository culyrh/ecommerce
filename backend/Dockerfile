# Multi-stage build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Download dependencies
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src src

# Build application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]