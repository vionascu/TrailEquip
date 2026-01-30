# TrailEquip Architecture

## System Overview

TrailEquip is a hiking trail discovery and planning application built with a clean, modular architecture designed for scalability and maintainability.

### Core Principles

- **Clean Architecture**: Strict separation between domain, application, and infrastructure layers
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **Domain-Driven Design**: Business logic concentrated in domain models
- **Testability**: All business logic is testable without external dependencies

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

## Component Architecture

### Domain Models

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

### Services

#### Infrastructure Services

**OverpassApiClient**
- Queries OpenStreetMap via Overpass API
- Implements rate limiting and retry logic
- Parses JSON responses into domain objects

#### Application Services

**TrailNormalizer**
- Converts OSM relations to Trail domain objects
- Parses OSMC trail marking symbols
- Infers difficulty from metrics
- Classifies terrain types
- Identifies hazards

**OSMIngestionService**
- Orchestrates trail ingestion pipeline
- Manages deduplication by OSM ID
- Validates trail data
- Updates existing trails from newer OSM versions
- Provides detailed ingestion statistics

**TrailExportService**
- Exports trails as GeoJSON (for web maps)
- Exports trails as GPX 1.1 (for GPS devices)
- Handles collections and single trails

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

## Data Flow

```
OpenStreetMap
    ↓
Overpass API
    ↓
OverpassApiClient (fetches relations)
    ↓
TrailNormalizer (converts to domain objects)
    ↓
OSMIngestionService (validates & deduplicates)
    ↓
PostgreSQL Database
    ↓
REST API ← → Frontend / GPS Apps
```

## API Contracts

### Ingestion Endpoints

```
POST /api/v1/osm/trails/ingest/bucegi
POST /api/v1/osm/trails/ingest/bbox
POST /api/v1/osm/trails/ingest/{osmRelationId}
POST /api/v1/osm/trails/ingest/nearby
```

### Export Endpoints

```
GET /api/v1/osm/trails/{id}/geojson
GET /api/v1/osm/trails/{id}/gpx
GET /api/v1/osm/trails/all/geojson
```

### Search Endpoints

```
GET /api/v1/osm/trails/search?q={query}
GET /api/v1/osm/trails/source/{source}
GET /api/v1/osm/trails/osm-id/{osmId}
GET /api/v1/osm/trails/health
```

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 21
- **Build**: Gradle (Kotlin DSL)
- **ORM**: Hibernate with Spatial support
- **Spatial**: PostGIS 3.3

### Database
- **PostgreSQL**: 15+
- **PostGIS**: 3.3+
- **Geometry Library**: JTS Core 1.19.0

### Testing
- **Framework**: JUnit 5
- **Mocking**: Mockito 4+
- **Coverage**: JaCoCo (80%+ minimum)

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

## Future Extensibility

The architecture is designed to support:

1. **Multiple Data Sources**: Trail data from different providers
2. **Different Export Formats**: KML, TopoJSON, etc.
3. **Advanced Filtering**: Complex geographic queries
4. **User Features**: Comments, ratings, trail conditions
5. **Real-time Updates**: WebSocket trail status updates
6. **Microservices**: Weather, Recommendations as separate services

---

For deployment and configuration details, see [STARTUP.md](STARTUP.md).
For API reference, see [API_REFERENCE.md](API_REFERENCE.md).
