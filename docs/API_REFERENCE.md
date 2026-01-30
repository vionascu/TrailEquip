# TrailEquip API Reference

## Base URL

```
http://localhost:8080/api/v1
```

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

## Swagger/OpenAPI Documentation

View interactive API documentation:

```
http://localhost:8080/swagger-ui.html
```

---

**For configuration, see [CONFIGURATION.md](CONFIGURATION.md)**
**For startup instructions, see [STARTUP.md](STARTUP.md)**
