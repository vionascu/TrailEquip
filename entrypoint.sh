#!/bin/sh
set -e

# Render/Railway startup script
# Converts DATABASE_URL to SPRING_DATASOURCE_URL before launching Java

echo "🚀 TrailEquip starting..."

# Check if DATABASE_URL is set (Render/Railway environment)
if [ -n "$DATABASE_URL" ]; then
  echo "✅ DATABASE_URL detected: $DATABASE_URL"

  # Convert postgresql://user:pass@host/db to jdbc:postgresql://host:5432/db?sslmode=require
  # (Render's DATABASE_URL doesn't include port, so we need to add it)
  if echo "$DATABASE_URL" | grep -q "^postgresql://"; then
    # Parse URL: postgresql://user:pass@host/database
    # Extract components using sed
    USER_PASS=$(echo "$DATABASE_URL" | sed 's|.*://\(.*\)@.*|\1|')
    HOST_DB=$(echo "$DATABASE_URL" | sed 's|.*@\(.*\)|\1|')
    HOST=$(echo "$HOST_DB" | sed 's|/.*||')
    DATABASE=$(echo "$HOST_DB" | sed 's|.*/||')

    # Check if host already has port, if not add default port 5432
    if echo "$HOST" | grep -q ":"; then
      # Host already has port
      SPRING_DATASOURCE_URL="jdbc:postgresql://${HOST}/${DATABASE}?sslmode=require"
    else
      # Add default PostgreSQL port
      SPRING_DATASOURCE_URL="jdbc:postgresql://${HOST}:5432/${DATABASE}?sslmode=require"
    fi

    echo "🔌 Converted to JDBC format: $SPRING_DATASOURCE_URL" | sed 's/:\/\/.*@/:\/\/***@/'
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
echo "📋 Environment Variables:"
echo "   DATABASE_URL: $(echo $DATABASE_URL | sed 's/:\/\/.*@/:\/\/***@/')"
echo "   SPRING_DATASOURCE_URL: $(echo $SPRING_DATASOURCE_URL | sed 's/:\/\/.*@/:\/\/***@/')"
echo "   PORT: ${PORT:-8080}"
echo "   SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-default}"
echo "   JAVA_OPTS: ${JAVA_OPTS}"
echo ""

# Build Java command with explicit datasource property as fallback
JAVA_DATASOURCE_PROP=""
if [ -n "$SPRING_DATASOURCE_URL" ]; then
  JAVA_DATASOURCE_PROP="-Dspring.datasource.url=$SPRING_DATASOURCE_URL"
fi

echo "📝 Datasource property flag: $JAVA_DATASOURCE_PROP"
echo ""

# Run Java with both environment variable and system property
exec java ${JAVA_OPTS} \
  -Dserver.port=${PORT:-8080} \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-render} \
  $JAVA_DATASOURCE_PROP \
  -jar app.jar
