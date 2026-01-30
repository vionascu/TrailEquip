# Configuration & Environment Variables

## Configuration Overview

TrailEquip uses a three-tier configuration system with clear precedence:

1. **Environment Variables** (highest priority)
2. **.env file** (project root)
3. **application.yml** (application defaults - lowest priority)

## Required Environment Variables

All these variables are **validated on startup**. If missing or invalid, startup **fails immediately**.

### Database Configuration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DATABASE_URL` | YES | - | PostgreSQL JDBC URL |
| `POSTGRES_DB` | YES | - | Database name |
| `POSTGRES_USER` | YES | - | Database user |
| `POSTGRES_PASSWORD` | YES | - | Database password |

### Example

```bash
export DATABASE_URL=postgresql://localhost:5432/trailequip
export POSTGRES_DB=trailequip
export POSTGRES_USER=trailequip
export POSTGRES_PASSWORD=secure_password_123
```

### OSM Integration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `OVERPASS_TIMEOUT` | NO | 60000 | Overpass API timeout (ms) |
| `OVERPASS_RATE_LIMIT` | NO | 3000 | Rate limit between requests (ms) |

### Example

```bash
export OVERPASS_TIMEOUT=60000
export OVERPASS_RATE_LIMIT=3000
```

### Spring Boot

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | NO | prod | Active Spring profile |
| `SERVER_PORT` | NO | 8081 | Server listening port |
| `LOGGING_LEVEL_ROOT` | NO | INFO | Root logging level |

### Example

```bash
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8081
export LOGGING_LEVEL_ROOT=INFO
```

## Creating .env File

Create `.env` file in project root (not committed to git):

```env
# ========== DATABASE ==========
DATABASE_URL=postgresql://localhost:5432/trailequip
POSTGRES_DB=trailequip
POSTGRES_USER=trailequip
POSTGRES_PASSWORD=your_secure_password_here

# ========== OVERPASS API ==========
OVERPASS_TIMEOUT=60000
OVERPASS_RATE_LIMIT=3000

# ========== SPRING BOOT ==========
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8081

# ========== LOGGING ==========
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TRAILEQUIP=DEBUG
```

## Configuration Files

### application.yml (Default)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.spatial.dialect.postgis.PostgisPG15Dialect
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/trailequip}
    username: ${POSTGRES_USER:trailequip}
    password: ${POSTGRES_PASSWORD:trailequip}

server:
  port: ${SERVER_PORT:8081}

logging:
  level:
    root: ${LOGGING_LEVEL_ROOT:INFO}
    com.trailequip: ${LOGGING_LEVEL_COM_TRAILEQUIP:INFO}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

### application-dev.yml (Development)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

logging:
  level:
    root: DEBUG
    com.trailequip: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### application-prod.yml (Production)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
      format_sql: false
    show-sql: false

logging:
  level:
    root: WARN
    com.trailequip: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
      base-path: /actuator
```

## Setting Environment Variables

### Method 1: Shell Export

```bash
export POSTGRES_PASSWORD=secure_password
export DATABASE_URL=postgresql://localhost:5432/trailequip
docker-compose up
```

### Method 2: .env File

Create `.env`:
```env
POSTGRES_PASSWORD=secure_password
DATABASE_URL=postgresql://localhost:5432/trailequip
```

Then:
```bash
docker-compose up
```

### Method 3: docker-compose.yml

```yaml
services:
  trail-service:
    environment:
      POSTGRES_PASSWORD: secure_password
      DATABASE_URL: postgresql://db:5432/trailequip
```

## Configuration Validation

### On Startup

The application validates:

1. **Database Connection**
   ```
   Attempting to connect to DATABASE_URL...
   ✓ PostgreSQL connected
   ```

2. **PostGIS Extension**
   ```
   Checking PostGIS installation...
   ✓ PostGIS 3.3 active
   ```

3. **Required Environment Variables**
   ```
   Validating environment variables...
   ✓ All required variables set
   ```

4. **Configuration Consistency**
   ```
   Checking configuration consistency...
   ✓ Configuration valid
   ```

### If Validation Fails

Application logs and fails immediately:

```
ERROR: DATABASE_URL environment variable not set
ERROR: Cannot start application without required configuration
Process exiting with code 1
```

## Database Connection Strings

### PostgreSQL Local

```
postgresql://localhost:5432/trailequip
```

### PostgreSQL Docker

```
postgresql://db:5432/trailequip
```

### PostgreSQL Remote

```
postgresql://user:password@remote.host:5432/database
```

### PostgreSQL with SSL

```
postgresql://user:password@remote.host:5432/database?sslmode=require
```

## Logging Configuration

### Log Levels

| Level | Usage |
|-------|-------|
| DEBUG | Development only, verbose output |
| INFO | Normal operation info |
| WARN | Recoverable warnings |
| ERROR | Failures |

### Configure Logging

```bash
# Set root logging to DEBUG
export LOGGING_LEVEL_ROOT=DEBUG

# Set application logging to INFO
export LOGGING_LEVEL_COM_TRAILEQUIP=INFO

# Set SQL logging
export LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG
```

## Profiles

### Development Profile

```bash
export SPRING_PROFILES_ACTIVE=dev
docker-compose up
```

Enables:
- H2 console
- Detailed logging
- Full SQL logging
- Swagger UI

### Production Profile

```bash
export SPRING_PROFILES_ACTIVE=prod
docker-compose up -d
```

Enables:
- Optimized performance
- Minimal logging
- Security headers
- Health checks only

## Overpass API Configuration

### Timeout

Maximum time to wait for Overpass API response (milliseconds):

```bash
export OVERPASS_TIMEOUT=60000  # 60 seconds
```

### Rate Limiting

Minimum delay between consecutive Overpass API requests (milliseconds):

```bash
export OVERPASS_RATE_LIMIT=3000  # 3 seconds
```

**Note**: Overpass API terms of service require rate limiting. Do not set below 2000ms.

## Troubleshooting Configuration

### "DATABASE_URL not recognized"

```bash
# Verify variable is set
echo $DATABASE_URL

# If empty, check .env file exists
cat .env

# Set manually
export DATABASE_URL=postgresql://localhost:5432/trailequip
```

### "PostGIS not found"

Ensure PostgreSQL image includes PostGIS:

```bash
docker-compose exec db psql -U trailequip -d trailequip \
  -c "SELECT PostGIS_version();"
```

### "Connection refused"

```bash
# Check database service
docker-compose ps db

# Check port accessibility
telnet localhost 5432

# Restart database
docker-compose restart db
```

### "Configuration validation failed"

```bash
# Check Spring Boot startup logs
docker-compose logs trail-service | grep -i validation

# Verify all required variables
env | grep -E "POSTGRES|DATABASE|OVERPASS"
```

## Security Considerations

### Never Commit Secrets

1. **.env file** - Add to .gitignore
2. **docker-compose.yml** - Don't hardcode passwords
3. **application.yml** - Use ${ENV_VAR} placeholders

### .gitignore

```
.env
.env.local
*.secret
```

### Safe Configuration Pattern

```yaml
# ✓ GOOD - Uses environment variables
datasource:
  password: ${POSTGRES_PASSWORD}

# ✗ BAD - Hardcoded password
datasource:
  password: secret123
```

## Configuration Precedence Example

If you set:

```bash
# Environment variable (highest priority)
export LOGGING_LEVEL_ROOT=DEBUG

# .env file
LOGGING_LEVEL_ROOT=WARN

# application.yml
logging:
  level:
    root: INFO
```

**Result**: DEBUG (environment variable wins)

---

**For startup instructions, see [STARTUP.md](STARTUP.md)**
**For API reference, see [API_REFERENCE.md](API_REFERENCE.md)**
