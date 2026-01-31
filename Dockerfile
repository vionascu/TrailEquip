# Multi-stage build for TrailEquip application

# Stage 1: Build frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/ui
COPY ui/package*.json ./
RUN npm ci --prefer-offline --no-audit
COPY ui/ ./
RUN npm run build

# Stage 2: Build backend
FROM gradle:8.6-jdk21-jammy AS backend-builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test --no-daemon

# Stage 3: Runtime image for backend
FROM eclipse-temurin:21-jre-jammy

# Install required packages
RUN apt-get update && apt-get install -y \
    postgresql-client \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy built JAR from builder
COPY --from=backend-builder /app/services/trail-service/build/libs/*.jar app.jar

# Copy frontend static files
COPY --from=frontend-builder /app/ui/dist /app/static

# Change ownership to app user
RUN chown -R appuser:appuser /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Expose port (Cloud Run uses PORT environment variable, default to 8080)
EXPOSE 8080

# Set environment variables for deployment
ENV PORT=8080 \
    JAVA_OPTS="-Xmx512m -Xms256m"

# Run application with environment variable support
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default} -jar app.jar"]
