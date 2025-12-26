# Multi-stage build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Download dependencies
# gradle 명령어 직접 사용 (gradlew의 네트워크 다운로드 회피)
RUN gradle dependencies --no-daemon || return 0
# RUN ./gradlew dependencies --no-daemon || return 0

# add gradlew authority
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build application - gradle 명령어 사용
RUN gradle clean bootJar -x test --no-daemon
# RUN gradle clean bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user
RUN mkdir -p /app/logs && \
    groupadd -r spring && \
    useradd -r -g spring spring && \
    chown -R spring:spring /app
USER spring:spring

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]