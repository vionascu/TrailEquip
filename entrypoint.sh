#!/bin/sh
set -e

# Render/Railway startup script
# Converts DATABASE_URL to SPRING_DATASOURCE_URL before launching Java

echo "🚀 TrailEquip starting..."

# Check if DATABASE_URL is set (Render/Railway environment)
if [ -n "$DATABASE_URL" ]; then
  echo "✅ DATABASE_URL detected: $DATABASE_URL"

  # Convert postgresql://user:pass@host:port/db to jdbc:postgresql://...?sslmode=require
  if echo "$DATABASE_URL" | grep -q "^postgresql://"; then
    SPRING_DATASOURCE_URL="jdbc:${DATABASE_URL}?sslmode=require"
    echo "🔌 Converted to JDBC format: $SPRING_DATASOURCE_URL"
  else
    SPRING_DATASOURCE_URL="$DATABASE_URL"
    echo "ℹ️  Using DATABASE_URL as-is"
  fi

  # Export so Java/Spring can see it
  export SPRING_DATASOURCE_URL
  echo "✅ SPRING_DATASOURCE_URL exported for Spring"
else
  echo "ℹ️  DATABASE_URL not set - using local configuration"
fi

# Show what we're about to run
echo ""
echo "📋 Starting Java with:"
echo "   PORT: ${PORT:-8080}"
echo "   SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-default}"
echo "   JAVA_OPTS: ${JAVA_OPTS}"
echo ""

# Run Java
exec java ${JAVA_OPTS} -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default} -jar app.jar
