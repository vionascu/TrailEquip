# TrailEquip Quick Start Guide

This guide will help you get the TrailEquip application running in 5 minutes.

## Prerequisites Check

First, verify all requirements are installed:

```bash
./quick-check.sh
```

This will show you what's installed and what's running.

## Setup (3 Steps)

### Step 1: Ensure PostgreSQL is Running

```bash
brew services start postgresql@15
```

Verify it's running:
```bash
pg_isready -h localhost
```

Should output: `accepting connections`

### Step 2: Build & Run Backend (Terminal 2)

Open a new terminal and run:

```bash
cd /Users/viionascu/Projects/TrailEquip

# Build all services
./gradlew clean build -x test

# Start Trail Service
./gradlew :trail-service:bootRun
```

Wait for it to show:
```
Started TrailServiceApplication in X.XXX seconds (JVM running for Y.ZZZ)
```

You can verify the backend is running:
```bash
curl http://localhost:8081/actuator/health
```

### Step 3: Start Frontend (Terminal 3)

Open another terminal and run:

```bash
cd /Users/viionascu/Projects/TrailEquip/ui

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

You'll see:
```
  ➜  Local:   http://localhost:5173/
```

## Access the Application

Open your browser and navigate to:
- **Application**: http://localhost:5173
- **API Docs**: http://localhost:8081/swagger-ui.html
- **API Health**: http://localhost:8081/actuator/health

## Features Included

✅ **Elevation Profile Graphs** - Interactive line charts showing trail elevation data
✅ **Interactive Maps** - OpenStreetMap with Leaflet integration
✅ **Trail Statistics** - Distance, elevation gain/loss, max slope
✅ **Weather Forecast** - 7-day forecast with equipment recommendations
✅ **Trail Reports** - Report hazards like bear sightings or blocked trails
✅ **Real API Data** - All data from Spring Boot backend (no mock data)

## Troubleshooting

### PostgreSQL Connection Error

```
Could not connect to database
```

**Solution:**
```bash
brew services start postgresql@15
psql -U postgres  # Verify access
```

### Port Already in Use

**For backend (8081):**
```bash
lsof -i :8081
kill -9 <PID>
```

**For frontend (5173):**
```bash
lsof -i :5173
kill -9 <PID>
```

### Frontend Shows "No Trails" or API Error

This is normal! The frontend starts empty and fetches from the API. Make sure:
1. PostgreSQL is running
2. Backend is running on port 8081
3. The error message shows which step failed

### Gradle Build Fails

Clear the cache and try again:
```bash
./gradlew clean
./gradlew clean build -x test
```

## Monitoring Logs

**Backend logs:**
```bash
tail -f /tmp/trail-service.log
```

**Frontend development:**
The dev server output appears in the terminal where you ran `npm run dev`

## Database

The application uses PostgreSQL 17 with PostGIS spatial extension:
- **Database Name**: `trailequip`
- **User**: `trailequip`
- **Host**: `localhost:5432`
- **PostGIS Extension**: Required for geographic queries (installed automatically with setup)

Tables are automatically created on first run via Hibernate/JPA.

### System Requirements
- PostgreSQL 17 with PostGIS 3.6+ installed
- Java 21 or higher
- Node.js 18+

## Architecture

```
Frontend (React + TypeScript)
    ↓ HTTP REST API
Backend Services (Spring Boot microservices)
    ↓ SQL Queries
PostgreSQL Database
```

### Services

- **Trail Service** (Port 8081) - Main API for trail data, elevation profiles, waypoints
- **Weather Service** (Port 8082) - Weather forecasts and conditions
- **Recommendation Service** (Port 8083) - Equipment recommendations based on weather

Only Trail Service is required for the basic application.

## Next Steps

- Browse available trails on the map
- Select a trail to view:
  - Elevation profile graph
  - Trail statistics (distance, difficulty, elevation)
  - Weather-specific equipment recommendations
  - Recent hazard reports
- Report trail issues using the report feature
- Check API documentation at http://localhost:8081/swagger-ui.html

## Useful Commands

```bash
# Run tests
./gradlew test

# Format code
./gradlew spotlessApply

# View all Gradle tasks
./gradlew tasks

# Run specific service
./gradlew :weather-service:bootRun  # Port 8082
./gradlew :recommendation-service:bootRun  # Port 8083

# Stop all services
pkill -f "java"
pkill -f "npm run dev"
```

## Questions?

Check the [BACKEND_SETUP.md](./BACKEND_SETUP.md) for more detailed backend information.
