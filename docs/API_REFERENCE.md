# TrailEquip API Reference

## Overview

TrailEquip provides a comprehensive REST API with 20+ endpoints across 3 microservices:

1. **Trail Service** (8081) - Trail management, OSM integration, exports
2. **Weather Service** (8082) - Weather forecasting and provider management
3. **Recommendation Service** (8083) - Equipment recommendations

All requests route through the **API Gateway** (8080) using Spring Cloud Gateway.

## Base URL

```
http://localhost:8080/api/v1
```

## Service Architecture

| Service | Port | Base Path | Purpose |
|---------|------|-----------|---------|
| **API Gateway** | 8080 | `/` | Request routing, Swagger, health checks |
| **Trail Service** | 8081 | `/api/v1/trails`, `/api/v1/osm/trails` | Trail CRUD, OSM, export, search |
| **Weather Service** | 8082 | `/api/v1/weather` | Weather forecasts, provider info |
| **Recommendation Service** | 8083 | `/api/v1/recommendations` | Equipment recommendations |

## Authentication

Currently no authentication required. Future versions will use JWT tokens.

## Response Format

All responses are JSON:

```json
{
  "id": "uuid",
  "name": "Trail Name",
  "difficulty": "HARD",
  "distance": 40.98,
  ...
}
```

## Error Responses

### 400 Bad Request

```json
{
  "error": "Invalid parameter",
  "message": "Trail distance must be positive"
}
```

### 404 Not Found

```json
{
  "error": "Not found",
  "message": "Trail not found with ID: xyz"
}
```

### 500 Internal Server Error

```json
{
  "error": "Internal error",
  "message": "An unexpected error occurred"
}
```

---

## Ingestion Endpoints

### Ingest Bucegi Trails

Ingest all hiking trails from Bucegi Mountains region.

```
POST /osm/trails/ingest/bucegi
```

**Response** (200 OK):

```json
{
  "success": true,
  "fetched": 156,
  "normalized": 156,
  "deduplicated": 142,
  "created": 135,
  "updated": 7,
  "failed": 0
}
```

**Curl Example:**

```bash
curl -X POST http://localhost:8080/api/v1/osm/trails/ingest/bucegi
```

---

### Ingest Trails by Bounding Box

Ingest trails within a geographic region defined by bounding box.

```
POST /osm/trails/ingest/bbox?south=45.2&west=25.4&north=45.5&east=25.7
```

**Parameters:**
- `south` (required): Minimum latitude
- `west` (required): Minimum longitude
- `north` (required): Maximum latitude
- `east` (required): Maximum longitude

**Response** (200 OK):

```json
{
  "success": true,
  "fetched": 45,
  "normalized": 44,
  "deduplicated": 40,
  "created": 38,
  "updated": 2,
  "failed": 0
}
```

**Curl Example:**

```bash
curl -X POST "http://localhost:8080/api/v1/osm/trails/ingest/bbox?south=45.2&west=25.4&north=45.5&east=25.7"
```

---

### Ingest Single Trail by OSM ID

Ingest a specific trail by its OpenStreetMap relation ID.

```
POST /osm/trails/ingest/{osmRelationId}
```

**Parameters:**
- `osmRelationId` (path, required): OpenStreetMap relation ID

**Response** (201 Created):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "osmId": 12345678,
  "name": "Sample Trail",
  "difficulty": "HARD",
  "distance": 15.5,
  "elevationGain": 1200,
  "source": "openstreetmap"
}
```

**Curl Example:**

```bash
curl -X POST http://localhost:8080/api/v1/osm/trails/ingest/12345678
```

---

### Ingest Trails Nearby

Ingest trails within a radius of a specific coordinate.

```
POST /osm/trails/ingest/nearby?latitude=45.35&longitude=25.54&radius=10
```

**Parameters:**
- `latitude` (required): Center latitude (WGS84)
- `longitude` (required): Center longitude (WGS84)
- `radius` (optional, default: 10): Search radius in kilometers

**Response** (200 OK):

```json
{
  "success": true,
  "fetched": 28,
  "normalized": 27,
  "created": 25,
  "failed": 0
}
```

**Curl Example:**

```bash
curl -X POST "http://localhost:8080/api/v1/osm/trails/ingest/nearby?latitude=45.35&longitude=25.54&radius=15"
```

---

## Export Endpoints

### Export Trail as GeoJSON

Export a single trail as GeoJSON for use in web maps (Leaflet, MapBox).

```
GET /osm/trails/{id}/geojson
```

**Parameters:**
- `id` (path, required): Trail UUID

**Response** (200 OK, Content-Type: application/json):

```json
{
  "type": "Feature",
  "properties": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Sample Trail",
    "difficulty": "HARD",
    "distance": 15.5,
    "elevationGain": 1200,
    "terrain": ["forest", "alpine_meadow"],
    "hazards": ["exposure", "weather_dependent"]
  },
  "geometry": {
    "type": "LineString",
    "coordinates": [
      [25.540, 45.348, 950],
      [25.542, 45.350, 1000],
      [25.544, 45.352, 1050]
    ]
  }
}
```

**Curl Example:**

```bash
curl http://localhost:8080/api/v1/osm/trails/{trail-uuid}/geojson > trail.geojson
```

---

### Export Trail as GPX

Export a single trail as GPX 1.1 for use with GPS devices.

```
GET /osm/trails/{id}/gpx
```

**Parameters:**
- `id` (path, required): Trail UUID

**Response** (200 OK, Content-Type: application/xml):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="TrailEquip"
     xmlns="http://www.topografix.com/GPX/1/1">
  <metadata>
    <name>Sample Trail</name>
    <desc>Trail description</desc>
  </metadata>
  <trk>
    <name>Sample Trail</name>
    <trkseg>
      <trkpt lat="45.348" lon="25.540">
        <ele>950</ele>
      </trkpt>
      <trkpt lat="45.350" lon="25.542">
        <ele>1000</ele>
      </trkpt>
    </trkseg>
  </trk>
  <wpt lat="45.348" lon="25.540">
    <name>Start Point</name>
    <type>START</type>
  </wpt>
</gpx>
```

**Curl Example:**

```bash
curl http://localhost:8080/api/v1/osm/trails/{trail-uuid}/gpx > trail.gpx
```

---

### Export All Trails as GeoJSON

Export all trails (or filtered) as GeoJSON FeatureCollection.

```
GET /osm/trails/all/geojson?difficulty=HARD&source=openstreetmap
```

**Parameters:**
- `difficulty` (optional): Filter by difficulty (EASY, MEDIUM, HARD, ALPINE, SCRAMBLING)
- `source` (optional): Filter by source (openstreetmap, muntii-nostri.ro)

**Response** (200 OK):

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": { ... },
      "geometry": { ... }
    },
    ...
  ]
}
```

**Curl Example:**

```bash
curl "http://localhost:8080/api/v1/osm/trails/all/geojson?difficulty=HARD" > all-hard-trails.geojson
```

---

## Search & Filter Endpoints

### Search Trails by Name

Search trails by partial name match.

```
GET /osm/trails/search?q=Omu
```

**Parameters:**
- `q` (required): Search query (substring match, case-insensitive)

**Response** (200 OK):

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Omu Peak Loop",
    "difficulty": "MEDIUM",
    "distance": 12.5
  }
]
```

**Curl Example:**

```bash
curl "http://localhost:8080/api/v1/osm/trails/search?q=peak"
```

---

### Get Trails by Source

Get all trails from a specific data source.

```
GET /osm/trails/source/{source}
```

**Parameters:**
- `source` (path, required): Data source name

**Available Sources:**
- `openstreetmap`: OpenStreetMap trails
- `muntii-nostri.ro`: Muntii Nostri website

**Response** (200 OK):

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Trail 1",
    "source": "openstreetmap"
  },
  ...
]
```

**Curl Example:**

```bash
curl http://localhost:8080/api/v1/osm/trails/source/openstreetmap
```

---

### Get Trail by OSM ID

Retrieve a specific trail by its OpenStreetMap relation ID.

```
GET /osm/trails/osm-id/{osmId}
```

**Parameters:**
- `osmId` (path, required): OpenStreetMap relation ID

**Response** (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "osmId": 12345678,
  "name": "Sample Trail",
  "difficulty": "HARD",
  "distance": 15.5
}
```

**Curl Example:**

```bash
curl http://localhost:8080/api/v1/osm/trails/osm-id/12345678
```

---

### Health Check

Check OSM integration health status.

```
GET /osm/trails/health
```

**Response** (200 OK):

```json
{
  "service": "OSM Integration",
  "status": "UP",
  "totalTrails": 156,
  "osmTrails": 142
}
```

**Curl Example:**

```bash
curl http://localhost:8080/api/v1/osm/trails/health
```

---

## CRUD Endpoints (Standard)

### List All Trails

```
GET /trails
```

**Query Parameters:**
- `difficulty` (optional): Filter by difficulty

**Response** (200 OK):

```json
[
  {
    "id": "uuid",
    "name": "Trail Name",
    "difficulty": "HARD",
    "distance": 15.5
  },
  ...
]
```

---

### Get Trail by ID

```
GET /trails/{id}
```

**Response** (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Sample Trail",
  ...
}
```

---

### Delete Trail

```
DELETE /trails/{id}
```

**Response** (204 No Content)

---

## Data Models

### Trail

```json
{
  "id": "uuid",
  "osmId": 12345678,
  "name": "Trail Name",
  "description": "Trail description...",
  "ref": "01MN02",
  "distance": 40.98,
  "elevationGain": 2020,
  "elevationLoss": 1930,
  "durationMinutes": 825,
  "maxSlope": 45.0,
  "avgSlope": 18.5,
  "maxElevation": 2507,
  "terrain": ["forest", "alpine_meadow"],
  "difficulty": "HARD",
  "hazards": ["exposure", "bears"],
  "source": "openstreetmap",
  "marking": {
    "osmcSymbol": "blue:blue_stripe",
    "color": "BLUE",
    "shape": "STRIPE",
    "hexColor": "#0000FF"
  },
  "waypoints": [
    {
      "id": "uuid",
      "name": "Start",
      "type": "START",
      "latitude": 45.348,
      "longitude": 25.540,
      "elevation": 950
    }
  ],
  "createdAt": "2026-01-30T10:15:32Z",
  "updatedAt": "2026-01-30T10:15:32Z"
}
```

### Difficulty Levels

```
EASY       (🟢) - maxSlope ≤ 10%,  elevation ≤ 500m
MEDIUM     (🟡) - maxSlope ≤ 20%,  elevation ≤ 1500m
HARD       (🔴) - maxSlope ≤ 30%,  elevation ≤ 2500m
ALPINE     (🟣) - maxSlope ≤ 40%,  elevation ≤ 3000m
SCRAMBLING (🧗) - maxSlope > 50%,  elevation > 3000m
```

### Waypoint Types

```
START      - Trail start point (🟢)
END        - Trail end point (🔴)
PEAK       - Mountain summit (⛰️)
SHELTER    - Mountain refuge/cabin (🏠)
WATER      - Water source (💧)
JUNCTION   - Trail junction (⛳)
CAMPING    - Camping area (⛺)
VIEWPOINT  - Scenic viewpoint (🔭)
OTHER      - Other point of interest (📍)
```

### Terrain Types

```
FOREST              - Dense tree coverage
ALPINE_MEADOW       - High altitude grassland
EXPOSED_RIDGE       - Windy, exposed height
SCRAMBLE            - Rock scrambling
ROCK                - Technical rock climbing
WATER_CROSSING      - Stream/river crossing
LOOSE_ROCK          - Unstable terrain (scree)
PAVED               - Road/pavement
```

---

## Rate Limiting

The API does not currently enforce rate limiting on HTTP requests, but the Overpass API integration enforces:

- **Minimum 3 seconds** between consecutive Overpass API queries
- **60 second timeout** for each query

---

## Pagination

Currently no pagination implemented. Future versions will support:

```
GET /trails?page=0&size=20&sort=distance,desc
```

---

---

## Weather Service Endpoints

### Get Weather Forecast

Get detailed weather forecast for a specific location and date range.

```
GET /weather/forecast?lat=45.35&lon=25.54&startDate=2026-01-30&endDate=2026-02-02&timezone=Europe/Bucharest
```

**Parameters:**
- `lat` (required): Latitude in WGS84 (e.g., 45.35 for Bucegi Mountains)
- `lon` (required): Longitude in WGS84 (e.g., 25.54)
- `startDate` (required): ISO 8601 date format (e.g., 2026-01-30)
- `endDate` (required): ISO 8601 date format (e.g., 2026-02-02)
- `timezone` (optional): IANA timezone name. Default: `Europe/Bucharest`

**Response** (200 OK):

```json
{
  "location": {
    "latitude": 45.35,
    "longitude": 25.54
  },
  "forecastData": {
    "time": [
      "2026-01-30",
      "2026-01-31",
      "2026-02-01",
      "2026-02-02"
    ],
    "temperature_2m_max": [8, 10, 12, 7],
    "temperature_2m_min": [2, 3, 5, 1],
    "precipitation_sum": [0, 2.5, 8.3, 1.2],
    "wind_speed_10m_max": [25, 30, 35, 28]
  },
  "provider": "open-meteo",
  "cached": false,
  "cacheValidUntil": "2026-01-30T22:00:00Z"
}
```

**Response Fields:**
- `location` - Request location
- `forecastData` - Weather arrays (indexed by day)
  - `time` - ISO date strings
  - `temperature_2m_max/min` - Temperature in °C
  - `precipitation_sum` - Precipitation in mm
  - `wind_speed_10m_max` - Wind speed in km/h
- `provider` - "open-meteo" (Open-Meteo API)
- `cached` - Whether this result came from cache
- `cacheValidUntil` - Cache expiration timestamp (6-hour TTL)

**Curl Example:**

```bash
curl "http://localhost:8080/api/v1/weather/forecast?lat=45.35&lon=25.54&startDate=2026-01-30&endDate=2026-02-02"
```

---

### List Weather Providers

Get available weather data providers and their status.

```
GET /weather/providers
```

**Response** (200 OK):

```json
{
  "providers": [
    {
      "id": "open-meteo",
      "name": "Open-Meteo",
      "status": "active",
      "description": "Free weather API with no authentication required"
    }
  ]
}
```

**Curl Example:**

```bash
curl http://localhost:8080/api/v1/weather/providers
```

**Notes:**
- Open-Meteo is the current default provider
- Additional providers can be implemented without breaking existing clients
- Each provider returns data in the same format for consistency

---

## Recommendation Service Endpoints

### Get Equipment Recommendations

Get intelligent equipment recommendations based on trail conditions and weather forecast.

```
POST /recommendations/equipment
Content-Type: application/json

{
  "trailId": "550e8400-e29b-41d4-a716-446655440000",
  "forecastStart": "2026-01-30",
  "forecastEnd": "2026-02-02"
}
```

**Request Body:**
- `trailId` (required): UUID of the trail
- `forecastStart` (required): ISO date format start of forecast period
- `forecastEnd` (required): ISO date format end of forecast period

**Response** (200 OK):

```json
{
  "equipment": [
    {
      "category": "LAYERS",
      "items": [
        {
          "name": "Base Layer (Thermal/Merino)",
          "reason": "Expected temperatures 1-5°C"
        },
        {
          "name": "Mid-Layer (Fleece)",
          "reason": "Insulation needed for sustained activity"
        }
      ]
    },
    {
      "category": "OUTERWEAR",
      "items": [
        {
          "name": "Insulated Jacket",
          "reason": "Low temperatures at elevation (1800m+)"
        },
        {
          "name": "Rain Shell",
          "reason": "50% precipitation chance on forecast days"
        },
        {
          "name": "Gaiters",
          "reason": "Protection from snow and mud"
        }
      ]
    },
    {
      "category": "TRACTION",
      "items": [
        {
          "name": "Microspikes",
          "reason": "Combination of low temp (2-5°C) + precipitation creates ice hazard"
        },
        {
          "name": "Insulated Boots",
          "reason": "Extended time at altitude with low temps"
        }
      ]
    },
    {
      "category": "ACCESSORIES",
      "items": [
        {
          "name": "Warm Hat & Gloves",
          "reason": "Summit exposure with 30+ km/h winds"
        },
        {
          "name": "Sunscreen & Glasses",
          "reason": "High UV at elevation despite cool temps"
        }
      ]
    }
  ],
  "warnings": [
    "High wind expected on ridges (30+ km/h) - consider route timing",
    "Variable conditions - prepare for rapid changes in weather",
    "Trail difficulty HARD + winter conditions - experience required"
  ],
  "summary": "Comprehensive winter layering system with full precipitation protection. Microspikes essential for ice management. Exposure hazard on ridges - monitor wind conditions."
}
```

**Response Fields:**
- `equipment` - Array of equipment categories with items
  - `category` - Equipment type (LAYERS, OUTERWEAR, TRACTION, ACCESSORIES, SPECIALIZED)
  - `items` - List of specific recommendations
    - `name` - Equipment item name
    - `reason` - Explanation for recommendation
- `warnings` - Array of important safety considerations
- `summary` - High-level overview of recommended gear strategy

**Equipment Categories:**

| Category | Examples | When Used |
|----------|----------|-----------|
| **LAYERS** | Base layers, mid-layers, insulation | Temperature < 15°C or sustained activity |
| **OUTERWEAR** | Rain shells, insulated jackets, gaiters | Precipitation or wind > 20 km/h |
| **TRACTION** | Microspikes, crampons, trail shoes | Ice risk or snow coverage |
| **ACCESSORIES** | Hat, gloves, sunscreen, goggles | Altitude, UV exposure, wind |
| **SPECIALIZED** | Climbing gear, avalanche equipment | Technical terrain or hazard conditions |

**Curl Example:**

```bash
curl -X POST http://localhost:8080/api/v1/recommendations/equipment \
  -H "Content-Type: application/json" \
  -d '{
    "trailId": "550e8400-e29b-41d4-a716-446655440000",
    "forecastStart": "2026-01-30",
    "forecastEnd": "2026-02-02"
  }'
```

**Recommendation Logic:**

The service analyzes:

1. **Temperature Analysis** (from forecast)
   - 0-5°C → thermal base layer + insulation
   - 5-10°C → fleece mid-layer
   - 10-15°C → wind layer
   - 15°C+ → minimal layering

2. **Precipitation Assessment**
   - 0-20% → no additional gear
   - 20-50% → rain shell
   - 50%+ → full rain gear (jacket + pants)

3. **Wind Evaluation**
   - < 20 km/h → minimal impact
   - 20-30 km/h → consider timing of ridge traversal
   - > 30 km/h → consider cancellation or route change

4. **Terrain Mapping**
   - Scrambling trails → climbing protection
   - Alpine terrain → avalanche assessment
   - Exposed ridges → wind/lightning awareness

5. **Temperature + Precipitation Combination**
   - Low temp + precipitation = ice hazard → microspikes
   - High altitude + low temp = cold injury risk → insulation + movement

---

## Swagger/OpenAPI Documentation

View interactive API documentation:

```
http://localhost:8080/swagger-ui.html
```

All endpoints are documented with:
- Endpoint descriptions
- Request/response schemas
- Example values
- Try-it-out functionality

---

## Health Check Endpoints

### Gateway Health

```
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

### Trail Service Health

```
GET /osm/trails/health
```

**Response:**
```json
{
  "service": "OSM Integration",
  "status": "UP",
  "totalTrails": 142,
  "osmTrails": 135
}
```

---

## Error Handling

### Common HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| **200** | Success | Trail found, forecast retrieved |
| **201** | Created | New trail ingested |
| **204** | No Content | Trail deleted successfully |
| **400** | Bad Request | Invalid query parameters |
| **404** | Not Found | Trail ID doesn't exist |
| **500** | Server Error | Database connection lost |

### Error Response Format

```json
{
  "error": "Trail not found",
  "message": "No trail exists with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-30T10:15:32Z",
  "status": 404
}
```

---

**For configuration, see [CONFIGURATION.md](CONFIGURATION.md)**
**For startup instructions, see [STARTUP.md](STARTUP.md)**
**For architecture details, see [ARCHITECTURE.md](ARCHITECTURE.md)**
