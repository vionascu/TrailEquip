# Local Startup Guide - TrailEquip

## Current Local Environment Status

| Component | Status | Version | Location |
|-----------|--------|---------|----------|
| Java | ✅ Ready | OpenJDK 21.0.10 | `/opt/homebrew/Cellar/openjdk/21.0.10` |
| Gradle | ✅ Ready | 8.14.4 | `/opt/homebrew/Cellar/gradle@8/8.14.4` |
| PostgreSQL | ✅ Running | 14.20 | Port 5432 |
| PostGIS | ⚠️ Incompatible | 3.6.1 (PG 17/18 only) | - |
| Docker | ❌ Not Available | - | - |
| Build Artifacts | ✅ Generated | 163 MB | `services/*/build/libs/` |

## Project Build Status

**Last Build**: January 30, 2026
**Build Result**: ✅ SUCCESS (58 seconds)
**Compiled Services**:
- ✅ api-gateway-0.1.0-SNAPSHOT.jar
- ✅ trail-service-0.1.0-SNAPSHOT.jar
- ✅ weather-service-0.1.0-SNAPSHOT.jar
- ✅ recommendation-service-0.1.0-SNAPSHOT.jar

## Prerequisites Setup

### 1. Set Java Home

Add this to your shell profile or run in current terminal:

```bash
export JAVA_HOME=$(brew --prefix openjdk@21)
```

Verify:
```bash
echo $JAVA_HOME
java -version
```

### 2. PostgreSQL Status

Check if running:
```bash
psql -l
```

If not running:
```bash
pg_ctl -D /opt/homebrew/var/postgresql@14 start
```

Verify database exists:
```bash
psql -d trailequip -c "\dt"
```

## Path to Production: Three Options

### ⭐ OPTION 1: Docker Compose (Recommended)

**When available**, use Docker for full infrastructure:

```bash
cd /Users/viionascu/Projects/TrailEquip
docker-compose up -d
sleep 30
curl http://localhost:8080/api/v1/osm/trails/health
open http://localhost:3000
```

**Advantages**:
- Full stack (PostgreSQL + PostGIS + all services)
- Reproducible environment
- CI/CD compatible

**Blockers**: Docker not currently available

---

### OPTION 2: Upgrade PostgreSQL to 18 (With PostGIS)

**If you want to continue with local PostgreSQL**:

```bash
# Step 1: Stop current PostgreSQL
pg_ctl -D /opt/homebrew/var/postgresql@14 stop

# Step 2: Back up data (optional, if needed)
# pg_dump -U viionascu trailequip > trailequip_backup.sql

# Step 3: Install PostgreSQL 18
brew install postgresql@18

# Step 4: Initialize database
/opt/homebrew/Cellar/postgresql@18/18.x.x/bin/initdb -D /opt/homebrew/var/postgresql@18

# Step 5: Start PostgreSQL 18
pg_ctl -D /opt/homebrew/var/postgresql@18 start

# Step 6: Create extension
psql -d trailequip -c "CREATE EXTENSION IF NOT EXISTS postgis;"

# Step 7: Verify
psql -d trailequip -c "SELECT PostGIS_version();"
```

**Advantages**:
- Homebrew native support for PostgreSQL 18 + PostGIS
- Full local development capability

**Considerations**:
- Need to migrate database from PG 14
- Different PostgreSQL version than production likely

---

### OPTION 3: Bypass PostGIS Validation (Development Mode)

**Temporary workaround for quick testing**:

#### 3a. Modify StartupValidator

Edit: `services/trail-service/src/main/java/com/trailequip/trail/infrastructure/config/StartupValidator.java`

Comment out PostGIS check:

```java
// Temporarily comment for local development
// validatePostGISExtension();
```

#### 3b. Rebuild

```bash
export JAVA_HOME=$(brew --prefix openjdk@21)
/opt/homebrew/Cellar/gradle@8/8.14.4/bin/gradle build -x test -x spotlessCheck
```

#### 3c. Set Environment Variables

```bash
export JAVA_HOME=$(brew --prefix openjdk@21)
export DATABASE_URL=jdbc:postgresql://localhost:5432/trailequip
export POSTGRES_DB=trailequip
export POSTGRES_USER=viionascu
export POSTGRES_PASSWORD=
export SPRING_PROFILES_ACTIVE=dev
export OVERPASS_TIMEOUT=60000
export OVERPASS_RATE_LIMIT=3000
```

#### 3d. Start Trail Service

```bash
java -jar services/trail-service/build/libs/trail-service-0.1.0-SNAPSHOT.jar
```

#### 3e. Verify Startup

```bash
curl -s http://localhost:8081/actuator/health | jq .
```

**Advantages**:
- Quick local testing without PostGIS
- No database migrations needed
- Fast iteration during development

**Limitations**:
- PostGIS functionality disabled (no spatial queries)
- Not suitable for production
- Requires code modification

---

## Quick Start Checklist

### Before Starting Services

- [ ] Set `JAVA_HOME`: `export JAVA_HOME=$(brew --prefix openjdk@21)`
- [ ] Verify Java: `java -version` (should show version 21)
- [ ] Check PostgreSQL: `psql -l` (should list databases)
- [ ] Database exists: `psql -d trailequip -c "\dt"`

### Environment Variables

```bash
export JAVA_HOME=$(brew --prefix openjdk@21)
export DATABASE_URL=jdbc:postgresql://localhost:5432/trailequip
export POSTGRES_DB=trailequip
export POSTGRES_USER=viionascu
export POSTGRES_PASSWORD=
export SPRING_PROFILES_ACTIVE=dev
```

### Start Services (If using Option 3)

```bash
# Terminal 1: Start Trail Service
java -jar services/trail-service/build/libs/trail-service-0.1.0-SNAPSHOT.jar

# Terminal 2: Start Weather Service (optional)
java -jar services/weather-service/build/libs/weather-service-0.1.0-SNAPSHOT.jar

# Terminal 3: Start Recommendation Service (optional)
java -jar services/recommendation-service/build/libs/recommendation-service-0.1.0-SNAPSHOT.jar

# Terminal 4: Start API Gateway
java -jar services/api-gateway/build/libs/api-gateway-0.1.0-SNAPSHOT.jar
```

### Verify Services

```bash
# Health check
curl http://localhost:8080/api/v1/osm/trails/health

# Check individual services
curl http://localhost:8081/actuator/health  # Trail Service
curl http://localhost:8082/actuator/health  # Weather Service
curl http://localhost:8083/actuator/health  # Recommendation Service
```

## Service Ports

| Service | Port | Endpoint |
|---------|------|----------|
| API Gateway | 8080 | `http://localhost:8080` |
| Trail Service | 8081 | `http://localhost:8081` |
| Weather Service | 8082 | `http://localhost:8082` |
| Recommendation Service | 8083 | `http://localhost:8083` |
| PostgreSQL | 5432 | `localhost:5432` |
| React UI | 3000 | `http://localhost:3000` (when available) |

## Troubleshooting

### Java Not Found

```bash
export JAVA_HOME=$(brew --prefix openjdk@21)
which java
```

### PostgreSQL Connection Failed

```bash
# Check if running
lsof -i :5432

# Start if stopped
pg_ctl -D /opt/homebrew/var/postgresql@14 start

# Check logs
pg_ctl -D /opt/homebrew/var/postgresql@14 status
```

### Port Already in Use

```bash
# Find what's using port 8080
lsof -i :8080

# Kill process if needed (be careful!)
kill -9 <PID>
```

### Build Fails

```bash
export JAVA_HOME=$(brew --prefix openjdk@21)

# Clean build
/opt/homebrew/Cellar/gradle@8/8.14.4/bin/gradle clean build -x test -x spotlessCheck

# Or check what's wrong
/opt/homebrew/Cellar/gradle@8/8.14.4/bin/gradle build --stacktrace
```

### PostGIS Extension Error

**If you see**: `ERROR: function postgis_version() does not exist`

**Solution**:
1. Either use Option 1 (Docker) or Option 2 (PostgreSQL 18)
2. Or use Option 3 and comment out PostGIS validation

## Next Steps

### Recommended Path

1. **Immediate**: Use Option 3 for quick local testing
2. **Short-term**: Get Docker working for full environment
3. **Production**: Use Docker Compose with CI/CD pipeline

### Development Workflow

```bash
# 1. Make code changes
vim services/trail-service/src/main/java/...

# 2. Rebuild specific service
/opt/homebrew/Cellar/gradle@8/8.14.4/bin/gradle :trail-service:build -x test

# 3. Restart service
# Kill running process and start again

# 4. Test changes
curl http://localhost:8080/api/v1/osm/trails/health
```

## Build Performance

| Operation | Time | Command |
|-----------|------|---------|
| Full build | 58s | `gradle build -x test` |
| Rebuild (no change) | 5s | `gradle build -x test` |
| Single service build | 10s | `gradle :trail-service:build` |

## Documentation References

- **[docs/STARTUP.md](docs/STARTUP.md)** - Comprehensive startup guide
- **[docs/CONFIGURATION.md](docs/CONFIGURATION.md)** - Configuration options
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** - System architecture
- **[docs/API_REFERENCE.md](docs/API_REFERENCE.md)** - API endpoints
- **[BUILD_SUCCESS.md](BUILD_SUCCESS.md)** - Build compilation details

---

**Status**: Project compiled and ready for local testing
**Recommendation**: Follow Option 3 for immediate testing, then upgrade to Option 1 or 2 for full functionality
