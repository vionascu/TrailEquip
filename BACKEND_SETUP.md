# TrailEquip Backend Setup Guide

This guide explains how to set up and run the TrailEquip backend services to power the frontend application.

## Prerequisites

1. **Java 21 or higher** - Required for Spring Boot 3.2
   - Download from: https://adoptium.net/ or use `brew install openjdk@21`
   - Verify: `java -version`

2. **PostgreSQL 14+** - Database for trail data
   - Download from: https://www.postgresql.org/download/
   - Or use: `brew install postgresql@15`
   - Verify: `psql --version`

3. **Gradle 8.5+** - Build tool (included via wrapper)
   - The project includes `./gradlew` so you don't need to install Gradle separately

## Database Setup

### Step 1: Start PostgreSQL

**On macOS with Homebrew:**
```bash
brew services start postgresql@15
```

**On Linux:**
```bash
sudo systemctl start postgresql
```

**On Windows:**
- PostgreSQL should start automatically as a service

### Step 2: Create Database and User

```bash
# Connect to PostgreSQL
psql -U postgres

# Run these SQL commands in the psql prompt:
CREATE USER trailequip WITH PASSWORD 'trailequip_dev';
CREATE DATABASE trailequip OWNER trailequip;
GRANT ALL PRIVILEGES ON DATABASE trailequip TO trailequip;
```

Then exit psql with `\q`

## Building the Backend

Navigate to the project root and build all services:

```bash
cd /Users/viionascu/Projects/TrailEquip

# Build all services (this may take a few minutes on first run)
./gradlew clean build -x test

# Or build specific service:
./gradlew :trail-service:build -x test
./gradlew :weather-service:build -x test
./gradlew :recommendation-service:build -x test
```

## Running the Services

### Trail Service (Required for Frontend)

```bash
cd /Users/viionascu/Projects/TrailEquip

# Run trail-service on port 8081
./gradlew :trail-service:bootRun
```

This will:
- Initialize the PostgreSQL database schema
- Start the Spring Boot application on `http://localhost:8081`
- Enable REST API at `http://localhost:8081/api/v1/trails`

### Weather Service (Optional)

```bash
# In a new terminal
cd /Users/viionascu/Projects/TrailEquip

# Run weather-service on port 8082
./gradlew :weather-service:bootRun
```

### Recommendation Service (Optional)

```bash
# In a new terminal
cd /Users/viionascu/Projects/TrailEquip

# Run recommendation-service on port 8083
./gradlew :recommendation-service:bootRun
```

## Starting the Frontend

Once the backend is running, start the frontend in a new terminal:

```bash
cd /Users/viionascu/Projects/TrailEquip/ui

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

The frontend will be available at `http://localhost:5173`

## API Endpoints

### Trail Service (Port 8081)

**Get All Trails:**
```
GET http://localhost:8081/api/v1/trails
```

**Get Single Trail:**
```
GET http://localhost:8081/api/v1/trails/{id}
```

**Search Trails:**
```
GET http://localhost:8081/api/v1/osm/trails/search?q=Bucegi
```

**Export Trail as GeoJSON:**
```
GET http://localhost:8081/api/v1/osm/trails/{id}/geojson
```

**Ingest Trails from OpenStreetMap:**
```
POST http://localhost:8081/api/v1/osm/trails/ingest/bucegi
```

### API Documentation

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8081/v3/api-docs
- **Info Endpoint**: http://localhost:8081/

## Troubleshooting

### "Connection refused" on port 8081
- **Cause**: Backend service not running
- **Solution**: Start trail-service with `./gradlew :trail-service:bootRun`

### "Could not connect to database"
- **Cause**: PostgreSQL not running or database not created
- **Solution**:
  1. Start PostgreSQL: `brew services start postgresql@15`
  2. Create database: Follow "Database Setup" section above

### "Gradle build failed"
- **Cause**: Dependency resolution issue
- **Solution**:
  1. Clear Gradle cache: `./gradlew clean`
  2. Rebuild: `./gradlew build -x test`

### "Port 8081 already in use"
- **Cause**: Another service already using the port
- **Solution**:
  1. Find process: `lsof -i :8081` (macOS/Linux)
  2. Kill process: `kill -9 <PID>`
  3. Or change port in `application.yml`

## Database Schema

The database schema is automatically created by Hibernate on first run based on the JPA entities:

- **trails** - Main trail data
- **waypoints** - Trail waypoints with coordinates and elevation
- **trail_segments** - Trail segments with geometry
- **trail_marking** - OSMC trail marking information
- **trail_terrain** - Terrain types per trail
- **trail_hazards** - Hazard warnings per trail

## Performance Tips

1. **Use H2 Database for Development (No PostgreSQL)**
   - Edit `application.yml`: Change datasource to H2
   - Much faster for local development

2. **Disable Detailed Logging**
   - Edit `logging.level.com.trailequip: WARN` in `application.yml`

3. **Use Database Indexes**
   - Trails are indexed by: osm_id, difficulty, source
   - Pre-populated indices for faster queries

## Next Steps

1. Start PostgreSQL database
2. Build backend with `./gradlew clean build -x test`
3. Run trail-service with `./gradlew :trail-service:bootRun`
4. Start frontend with `npm run dev`
5. Open http://localhost:5173 in your browser

## Further Documentation

- **Trail Service**: See `services/trail-service/README.md`
- **API Endpoints**: Visit http://localhost:8081/swagger-ui.html
- **Architecture**: See `ARCHITECTURE.md`
