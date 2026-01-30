# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Download dependencies (cached layer)
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

# Build application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -g 1001 zevaro && \
    adduser -u 1001 -G zevaro -s /bin/sh -D zevaro

# Copy artifact
COPY --from=builder /app/target/zevaro-core-*.jar app.jar

# Set ownership
RUN chown -R zevaro:zevaro /app
USER zevaro

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
