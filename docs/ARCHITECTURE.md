# TrailEquip Architecture

## System Overview

TrailEquip is a comprehensive hiking trail discovery and planning application built with a microservices architecture. It integrates trail data from OpenStreetMap, provides weather forecasting, and offers intelligent equipment recommendations based on trail conditions.

**Microservices:**
- **Trail Service** (8081) - Trail CRUD, OSM integration, GeoJSON/GPX export
- **Weather Service** (8082) - Weather forecasting via Open-Meteo API
- **Recommendation Service** (8083) - Equipment recommendations based on trail and weather
- **API Gateway** (8080) - Central request routing and aggregation

### Core Principles

- **Clean Architecture**: Strict separation between domain, application, and infrastructure layers
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **Domain-Driven Design**: Business logic concentrated in domain models
- **Testability**: All business logic is testable without external dependencies
- **Microservices**: Independent services with clear contracts and separation of concerns

## Layer Architecture

```
┌─────────────────────────────────────────────────────┐
│                    REST API Layer                    │
│        (Controllers, DTOs, API Responses)            │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────┐
│              Application Services Layer              │
│    (Use cases, workflow orchestration, DTOs)        │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────┐
│                 Domain Layer                         │
│       (Entities, Value Objects, Domain Rules)       │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────┐
│              Infrastructure Layer                    │
│    (Persistence, External Services, Adapters)       │
└─────────────────────────────────────────────────────┘
```

## Microservices Overview

### Service Architecture

```
┌─────────────────────────────────────────────────────┐
│          API Gateway (Spring Cloud Gateway)          │
│                      Port 8080                       │
└──────┬────────────────┬────────────────┬────────────┘
       │                │                │
       ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Trail Service │  │Weather Svc   │  │Recommend Svc │
│    8081      │  │    8082      │  │    8083      │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                │                │
       ▼                ▼                ▼
   PostgreSQL    Open-Meteo API    Weather Cache
   + PostGIS     + Caching         + Logic
```

### Service Details

| Service | Port | Purpose | Key Features |
|---------|------|---------|--------------|
| **API Gateway** | 8080 | Central request routing | Route aggregation, health, metrics, Swagger |
| **Trail Service** | 8081 | Trail management | OSM ingestion, GeoJSON/GPX export, geospatial queries |
| **Weather Service** | 8082 | Weather forecasting | 6-hour caching, multi-location support, timezone support |
| **Recommendation Service** | 8083 | Equipment recommendations | Dynamic rules based on trail + weather data |

---

## Component Architecture

### Domain Models (Trail Service)

```
Trail (Root Aggregate)
├── id: UUID
├── osmId: Long
├── name: String
├── geometry: LineString (PostGIS)
├── difficulty: Difficulty (Enum)
├── distance: Double
├── elevationGain: Integer
├── elevationLoss: Integer
├── maxSlope: Double
├── avgSlope: Double
├── terrain: List<String>
├── hazards: List<String>
├── marking: TrailMarking (Value Object)
├── waypoints: List<Waypoint> (Child Entities)
└── segments: List<TrailSegment> (Child Entities)

TrailMarking (Value Object)
├── osmcSymbol: String
├── color: Enum
├── shape: Enum
└── hexColor: String

Waypoint (Entity)
├── id: UUID
├── trailId: UUID (FK)
├── osmNodeId: Long
├── latitude: Double
├── longitude: Double
├── elevation: Integer
├── name: String
├── type: Enum
└── description: String

TrailSegment (Entity)
├── id: UUID
├── trailId: UUID (FK)
├── osmWayId: Long
├── geometry: LineString
├── terrainType: Enum
├── length: Double
└── accessible: Boolean

Difficulty (Value Object)
├── EASY, MEDIUM, HARD, ALPINE, SCRAMBLING
├── maxSlopeThreshold: Double
└── maxElevationGainThreshold: Integer
```

### Trail Service - Key Components

#### Infrastructure Services

**OverpassApiClient**
- Queries OpenStreetMap via Overpass API
- Implements rate limiting (3-second minimum between requests)
- 60-second timeout per query
- Parses JSON responses into domain objects
- Supports bounding box and radius queries

#### Application Services

**TrailNormalizer**
- Converts OSM relations to Trail domain objects
- Parses OSMC trail marking symbols (blue, red, yellow, green, etc.)
- Infers difficulty from metrics (slope + elevation)
- Classifies terrain types (forest, alpine, exposed, etc.)
- Identifies hazards (exposure, loose rock, weather dependency)

**OSMIngestionService**
- Orchestrates trail ingestion pipeline
- Manages deduplication by OSM ID (prevents duplicates)
- Validates trail data before persistence
- Updates existing trails from newer OSM versions
- Provides detailed ingestion statistics (fetched, normalized, deduplicated, created, updated, failed)

**TrailExportService**
- Exports trails as GeoJSON (for web maps, Leaflet, MapBox)
- Exports trails as GPX 1.1 (for GPS devices, Garmin, Strava)
- Supports both single trails and collections
- Includes elevation, waypoints, and metadata

---

### Weather Service - Key Components

**WeatherController** (Port 8082)
- Receives forecast requests with location and date range
- Routes to weather provider implementations

**WeatherProvider Interface**
- Abstraction for different weather data sources
- Open-Meteo implementation (free, no API key required)
- Extensible for additional providers

**WeatherCaching**
- 6-hour cache validity for forecast data
- Cache key: `latitude-longitude-startDate-endDate`
- Configurable timezone support (default: Europe/Bucharest)
- Prevents duplicate external API calls

**Open-Meteo Integration**
- Free, open-source weather API (https://api.open-meteo.com)
- Provides temperature, precipitation, wind data
- No authentication required
- Response includes forecast arrays for multiple days

---

### Recommendation Service - Key Components

**RecommendationController** (Port 8083)
- Receives trail ID and forecast dates
- Integrates with Trail Service and Weather Service
- Returns categorized equipment recommendations

**RecommendationLogic**
- Temperature-based layer recommendations (0-5°C → thermal base layer)
- Precipitation logic (40%+ → rain shell, 60%+ → full rain gear)
- Wind-based suggestions (high wind → ridge caution warnings)
- Ice/traction assessment (temp + precipitation → microspikes)
- Terrain-specific equipment (scrambling → climbing gear)

**Equipment Categories**
- LAYERS: Base layers, mid-layers, insulation
- OUTERWEAR: Rain jackets, wind shells, gaiters
- TRACTION: Microspikes, crampons, trail shoes
- ACCESSORIES: Sun protection, insect repellent, etc.
- SPECIALIZED: Climbing gear, avalanche equipment based on trail type

## Database Schema

All tables use UUIDs for primary keys (except trail_markings which uses BIGSERIAL).

### PostGIS Integration

- All trail geometry stored as `GEOMETRY(LineString, 4326)`
- SRID 4326 = WGS84 (latitude/longitude)
- GIST spatial indexes for efficient geographic queries

### Key Tables

- `trails`: Main trail data with geometry
- `trail_markings`: OSMC marking symbols
- `trail_waypoints`: Intermediate points along trails
- `trail_segments`: Individual OSM ways
- `weather_cache`: Cached forecast data

## Data Flows

### Trail Ingestion Flow

```
OpenStreetMap
    ↓
Overpass API
    ↓
OverpassApiClient (fetches relations with rate limiting)
    ↓
TrailNormalizer (converts to domain objects, parses OSMC, infers difficulty)
    ↓
OSMIngestionService (validates, deduplicates by osmId, updates existing)
    ↓
PostgreSQL Database (with PostGIS indexes)
    ↓
REST API
```

### Trail Export Flow

```
REST API Request (GET /osm/trails/{id}/geojson or gpx)
    ↓
TrailExportService
    ↓
PostgreSQL Query (retrieve geometry + metadata)
    ↓
Format Conversion (GeoJSON or GPX 1.1)
    ↓
HTTP Response (JSON or XML)
```

### Weather Integration Flow

```
Frontend Request (/api/v1/weather/forecast)
    ↓
WeatherController (API Gateway → Port 8082)
    ↓
Cache Check (6-hour validity)
    ↓ (Cache miss)
Open-Meteo API Call
    ↓
WeatherProvider (parses forecast data)
    ↓
Cache Store (6-hour TTL)
    ↓
HTTP Response (forecast + cache timestamp)
```

### Recommendation Flow

```
Frontend Request (/api/v1/recommendations/equipment?trailId=X)
    ↓
RecommendationController (API Gateway → Port 8083)
    ↓
Fetch Trail Data (from Trail Service or DB)
    ↓
Fetch Weather Forecast (from Weather Service)
    ↓
Apply RecommendationLogic
    - Temperature analysis
    - Precipitation assessment
    - Wind evaluation
    - Terrain + difficulty mapping
    ↓
Generate Equipment List (categorized by type)
    ↓
Add Warnings & Summary
    ↓
HTTP Response (equipment recommendations)
```

## API Gateway (Port 8080)

### Configuration

Routes requests to microservices using Spring Cloud Gateway predicates:

```yaml
routes:
  - id: trail-service
    uri: http://localhost:8081
    predicates:
      - Path=/api/v1/trails/**

  - id: weather-service
    uri: http://localhost:8082
    predicates:
      - Path=/api/v1/weather/**

  - id: recommendation-service
    uri: http://localhost:8083
    predicates:
      - Path=/api/v1/recommendations/**
```

### Gateway Endpoints

**Swagger/OpenAPI:**
- `GET /swagger-ui.html` - Interactive API documentation
- `GET /v3/api-docs` - OpenAPI schema

**Health & Monitoring:**
- `GET /actuator/health` - Service health status
- `GET /actuator/metrics` - Application metrics

### Service-to-Service Communication

Services communicate via HTTP through:
1. **API Gateway routing** - For external client requests
2. **Direct HTTP calls** - For inter-service calls (Recommendation Service → Trail/Weather Services)
3. **External APIs** - Trail Service → Overpass API, Weather Service → Open-Meteo API

---

## API Contracts by Service

### Trail Service Endpoints (Port 8081)

#### Ingestion Endpoints

```
POST /api/v1/osm/trails/ingest/bucegi                    - Ingest Bucegi Mountains trails
POST /api/v1/osm/trails/ingest/bbox                      - Ingest by geographic bounding box
POST /api/v1/osm/trails/ingest/{osmRelationId}           - Ingest single trail by OSM ID
POST /api/v1/osm/trails/ingest/nearby                    - Ingest trails within radius
```

#### Export Endpoints

```
GET /api/v1/osm/trails/{id}/geojson                      - Export trail as GeoJSON
GET /api/v1/osm/trails/{id}/gpx                          - Export trail as GPX 1.1
GET /api/v1/osm/trails/all/geojson                       - Export all trails as FeatureCollection
```

#### Search & Metadata Endpoints

```
GET /api/v1/osm/trails/search?q={query}                  - Search by trail name
GET /api/v1/osm/trails/source/{source}                   - Get trails by data source
GET /api/v1/osm/trails/osm-id/{osmId}                    - Get trail by OSM relation ID
GET /api/v1/osm/trails/health                            - Health check with statistics
```

#### Standard CRUD Endpoints

```
GET /api/v1/trails                                        - List all trails (with optional difficulty filter)
GET /api/v1/trails/{id}                                  - Get specific trail
POST /api/v1/trails                                       - Create new trail
DELETE /api/v1/trails/{id}                                - Delete trail
GET /api/v1/trails/suggest                               - Geographic search within radius
```

### Weather Service Endpoints (Port 8082)

```
GET /api/v1/weather/forecast                             - Get weather forecast for coordinates and date range
GET /api/v1/weather/providers                            - List available weather providers
```

**Forecast Request Parameters:**
- `lat` (required) - Latitude in WGS84
- `lon` (required) - Longitude in WGS84
- `startDate` (required) - ISO date format
- `endDate` (required) - ISO date format
- `timezone` (optional) - Default: Europe/Bucharest

### Recommendation Service Endpoints (Port 8083)

```
POST /api/v1/recommendations/equipment                   - Get equipment recommendations
```

**Request Body:**
```json
{
  "trailId": "550e8400-e29b-41d4-a716-446655440000",
  "forecastStart": "2026-01-30",
  "forecastEnd": "2026-02-02"
}
```

## Technology Stack

### Backend Services
- **Framework**: Spring Boot 3.2.0+
- **Gateway**: Spring Cloud Gateway 4.0.7
- **Language**: Java 21 (OpenJDK 21.0.10)
- **Build**: Gradle 8.14.4 (Kotlin DSL)
- **ORM**: Hibernate 6.4.1 with Spatial support
- **Caching**: Spring Cache (configurable backend)
- **HTTP Client**: RestTemplate for inter-service communication

### Database & Spatial
- **PostgreSQL**: 14.20+ with HikariCP connection pooling
- **PostGIS**: 3.3+ (for geographic queries and geometry storage)
- **Geometry Library**: JTS Core 1.19.0 (Java Topology Suite)
- **Spatial Indexes**: GIST indexes on trail geometry for performance

### API Documentation & Routing
- **Swagger/OpenAPI**: SpringDoc 2.0.4
- **Service Discovery**: API Gateway with path-based routing
- **Health Checks**: Spring Boot Actuator (health, metrics)

### Testing & Quality
- **Framework**: JUnit 5
- **Mocking**: Mockito 4+
- **Coverage**: JaCoCo (80%+ minimum, enforced in CI/CD)
- **Code Format**: Spotless with Palantir Java Format

### External Services
- **Weather Data**: Open-Meteo API (free, no authentication)
- **Trail Data**: OpenStreetMap via Overpass API
- **Map Visualization**: Leaflet.js + React Leaflet (frontend)

## Error Handling

### Custom Exceptions

- `OverpassApiException`: OSM/Overpass API errors
- `ValidationException`: Data validation failures

### Error Response Format

All API errors follow a consistent format with HTTP status codes.

## Logging

### Levels

- **INFO**: Lifecycle events, ingestion start/stop
- **WARN**: Recoverable issues (invalid trail, parsing errors)
- **ERROR**: Failures requiring intervention

### No Secrets

- Never logs credentials, tokens, or sensitive data
- Logs support production debugging

## Performance Considerations

### Caching Strategy

1. **Weather Forecast Caching** (Weather Service)
   - 6-hour TTL per location + date range
   - Reduces external API calls
   - Cache invalidation on new forecast dates

2. **Database Indexing** (Trail Service)
   - GIST spatial index on trail geometry (PostGIS)
   - B-tree indexes on osm_id, difficulty, source
   - Composite indexes on common query combinations

3. **Connection Pooling**
   - HikariCP for PostgreSQL connections
   - Pool size: 10 connections per service
   - Reduces database connection overhead

### Rate Limiting

**Overpass API (OSM Integration):**
- Minimum 3-second delay between consecutive requests
- 60-second timeout per request
- Exponential backoff for rate-limit errors

**HTTP Requests:**
- No client-side rate limiting (future enhancement)
- API Gateway can add rate limiting filters if needed

---

## Future Extensibility

The architecture is designed to support:

1. **Multiple Weather Providers**: Alternative to Open-Meteo (DarkSky, NOAA, etc.)
2. **Different Export Formats**: KML, TopoJSON, Shapefile
3. **Advanced Filtering**: Complex geographic queries with polygons
4. **User Features**: Comments, ratings, trail condition reports
5. **Real-time Updates**: WebSocket for trail status and crowdedness
6. **User Authentication**: JWT-based auth with role-based access
7. **Trail Analytics**: Popular routes, trending trails, difficulty trends
8. **Mobile Optimization**: Offline map caching, mobile-specific endpoints
9. **Social Integration**: Share trails, create route collections
10. **API Versioning**: Support multiple API versions for backward compatibility

---

For deployment and configuration details, see [STARTUP.md](STARTUP.md).
For API reference, see [API_REFERENCE.md](API_REFERENCE.md).
