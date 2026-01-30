# TrailEquip - Startup & Restart Guide

## ONE-COMMAND STARTUP

The entire application starts with a single command via Docker Compose:

```bash
docker-compose up
```

This command:
1. Starts PostgreSQL with PostGIS
2. Creates database schema and seed data
3. Starts Trail Service (Spring Boot)
4. Starts API Gateway
5. Exposes all services on required ports

## Prerequisites

### System Requirements
- Docker Desktop 4.0+
- Docker Compose 2.0+
- 2GB RAM minimum
- 5GB disk space

### Installation

**macOS:**
```bash
brew install docker
brew install docker-compose
```

**Linux:**
```bash
sudo apt-get install docker.io docker-compose
```

**Windows:**
- Install Docker Desktop: https://www.docker.com/products/docker-desktop

## Startup Process

### 1. Start Services

```bash
cd /Users/viionascu/Projects/TrailEquip
docker-compose up
```

### 2. Verify Startup

Wait for logs showing:
```
trail-service    | Started TrailServiceApplication in X seconds
api-gateway      | Started ApiGateway in X seconds
```

### 3. Verify All Services

```bash
# Check services are running
docker-compose ps

# Expected output:
# NAME                 STATUS        PORTS
# trailequip-db        Up            5432
# trail-service        Up            8081
# api-gateway          Up            8080
# react-ui             Up            3000
```

## Health Checks

### API Gateway Health
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status": "UP"}
```

### Trail Service Health
```bash
curl http://localhost:8081/actuator/health
```

### OSM Integration Health
```bash
curl http://localhost:8080/api/v1/osm/trails/health
```

Expected response:
```json
{
  "service": "OSM Integration",
  "status": "UP",
  "totalTrails": 3,
  "osmTrails": 3
}
```

## Configuration & Environment Variables

### Required Environment Variables

These are validated on startup and must be set:

```bash
# PostgreSQL
POSTGRES_DB=trailequip
POSTGRES_USER=trailequip
POSTGRES_PASSWORD=trailequip
DATABASE_URL=postgresql://localhost:5432/trailequip

# Spring Boot
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8081

# Overpass API
OVERPASS_TIMEOUT=60000
OVERPASS_RATE_LIMIT=3000
```

### Configuration Loading Order

1. **Environment Variables** (highest priority)
2. **.env file** (if present in project root)
3. **application.yml** (application defaults)

### Sample .env File

Create `.env` in project root:

```env
# Database
POSTGRES_DB=trailequip
POSTGRES_USER=trailequip
POSTGRES_PASSWORD=your_secure_password_here
DATABASE_URL=postgresql://localhost:5432/trailequip

# Overpass API
OVERPASS_TIMEOUT=60000
OVERPASS_RATE_LIMIT=3000

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TRAILEQUIP=DEBUG
```

## Restart Procedure

### Graceful Restart (Preserve Data)

```bash
# Stop services
docker-compose down

# Start services again
docker-compose up
```

### Full Restart (Fresh Database)

```bash
# Stop and remove all volumes
docker-compose down -v

# Start services (will recreate database)
docker-compose up
```

### Restart Single Service

```bash
# Restart Trail Service
docker-compose restart trail-service

# Restart PostgreSQL
docker-compose restart db
```

## Startup Validation

The application validates on startup:

### ✅ Validated on Every Startup

- PostgreSQL connection
- PostGIS extension availability
- Database schema exists
- Required tables created
- Required environment variables set
- Configuration consistency
- API Gateway connectivity

### ❌ Fails Fast If

- PostgreSQL unreachable
- Database user invalid
- PostGIS not installed
- Schema migration fails
- Required env vars missing
- Port already in use

### 📋 Startup Logs

Watch logs for validation messages:

```
2026-01-30 10:15:32 INFO  Starting TrailEquipApplication
2026-01-30 10:15:33 INFO  Validating PostgreSQL connection...
2026-01-30 10:15:33 INFO  PostgreSQL connected ✓
2026-01-30 10:15:33 INFO  Checking PostGIS extension...
2026-01-30 10:15:33 INFO  PostGIS 3.3 active ✓
2026-01-30 10:15:34 INFO  Validating environment variables...
2026-01-30 10:15:34 INFO  All required environment variables set ✓
2026-01-30 10:15:35 INFO  Started TrailEquipApplication in 2.5 seconds
```

## Port Configuration

### Default Ports

| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL | 5432 | Database |
| Trail Service | 8081 | REST API |
| API Gateway | 8080 | Public API endpoint |
| React UI | 3000 | Web interface |

### Changing Ports

Edit `docker-compose.yml`:

```yaml
services:
  trail-service:
    ports:
      - "9081:8081"  # Change from 8081 to 9081
```

Then restart:
```bash
docker-compose down
docker-compose up
```

## Troubleshooting Startup

### "Port already in use"

```bash
# Find what's using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in docker-compose.yml
```

### "PostgreSQL connection refused"

```bash
# Check database service
docker-compose logs db

# Restart database
docker-compose restart db
```

### "PostGIS not found"

```bash
# Verify PostGIS installed
docker-compose exec db psql -U trailequip -d trailequip \
  -c "SELECT PostGIS_version();"
```

### "Configuration validation failed"

Check logs:
```bash
docker-compose logs trail-service | grep -i "validation\|error"
```

### "Overpass API timeout"

Check internet connection and Overpass API status:
```bash
# Verify Overpass API is accessible
curl https://overpass-api.de/api/status
```

## Accessing Services

### REST API (via API Gateway)

```bash
# Base URL
http://localhost:8080/api/v1

# List all trails
curl http://localhost:8080/api/v1/trails

# Get trail by ID
curl http://localhost:8080/api/v1/trails/{id}/geojson
```

### Direct Trail Service

```bash
# Trail Service (internal)
curl http://localhost:8081/api/v1/trails
```

### Database Access

```bash
# Connect to PostgreSQL
psql -U trailequip -h localhost -d trailequip

# List tables
\dt

# Query trails
SELECT id, name, difficulty FROM trails;
```

### React UI

Open browser:
```
http://localhost:3000
```

## Seed Data

On first startup, the database is populated with:

1. **Trail Markings** (6 OSMC symbols)
   - Blue stripe, Red triangle, Yellow cross, Green dot, White stripe, Orange rectangle

2. **Sample Trails** (3 trails)
   - Omu Peak Loop (MEDIUM, 12.5 km)
   - Sphinx Ridge Scramble (HARD, 8.3 km)
   - Bulea Lake Forest Walk (EASY, 6.8 km)

## Monitoring & Logs

### View Service Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f trail-service

# Follow last 100 lines
docker-compose logs -f --tail=100 trail-service
```

### Check Service Status

```bash
# Overall status
docker-compose ps

# Detailed health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

## Development vs Production

### Development Startup

```bash
# Use application-dev.yml
export SPRING_PROFILES_ACTIVE=dev
docker-compose up
```

### Production Startup

```bash
# Use application-prod.yml
export SPRING_PROFILES_ACTIVE=prod
docker-compose up -d
```

## Next Steps

After successful startup:

1. Verify all health checks pass
2. Test API endpoints (see API_REFERENCE.md)
3. Ingest trails from OSM (see INGESTION.md)
4. Check database (see DATABASE.md)

---

**For configuration details, see [CONFIGURATION.md](CONFIGURATION.md)**
**For API reference, see [API_REFERENCE.md](API_REFERENCE.md)**
