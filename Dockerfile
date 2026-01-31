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

# Create startup script that converts DATABASE_URL to JDBC format
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'set -e' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Convert DATABASE_URL to SPRING_DATASOURCE_URL if set' >> /app/entrypoint.sh && \
    echo 'if [ -n "$DATABASE_URL" ]; then' >> /app/entrypoint.sh && \
    echo '  # DATABASE_URL format: postgresql://user:pass@host:port/dbname' >> /app/entrypoint.sh && \
    echo '  # Convert to: jdbc:postgresql://user:pass@host:port/dbname?sslmode=require' >> /app/entrypoint.sh && \
    echo '  if echo "$DATABASE_URL" | grep -q "^postgresql://"; then' >> /app/entrypoint.sh && \
    echo '    SPRING_DATASOURCE_URL="jdbc:${DATABASE_URL}?sslmode=require"' >> /app/entrypoint.sh && \
    echo '  else' >> /app/entrypoint.sh && \
    echo '    SPRING_DATASOURCE_URL="$DATABASE_URL"' >> /app/entrypoint.sh && \
    echo '  fi' >> /app/entrypoint.sh && \
    echo '  export SPRING_DATASOURCE_URL' >> /app/entrypoint.sh && \
    echo '  echo "✅ SPRING_DATASOURCE_URL set from DATABASE_URL"' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo 'exec java ${JAVA_OPTS} -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default} -jar app.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
