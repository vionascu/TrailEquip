# TrailEquip OSM Integration - Quick Start Guide

## 🚀 Getting Started

### 1. Start the Services
```bash
cd /Users/viionascu/Projects/TrailEquip
docker-compose up -d
```

### 2. Verify Database
```bash
# Connect to PostgreSQL
psql -U trailequip -d trailequip

# Check PostGIS is enabled
SELECT PostGIS_version();

# Check tables created
\dt

# Check trails
SELECT id, name, difficulty FROM trails;
```

### 3. Check API Health
```bash
curl http://localhost:8080/api/v1/osm/trails/health
```

---

## 📍 Common Tasks

### Ingest Trails from Bucegi Mountains
```bash
curl -X POST http://localhost:8080/api/v1/osm/trails/ingest/bucegi
```

Response:
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

### Ingest Trails by Geographic Region
```bash
# Bucegi Mountains bounding box
curl -X POST "http://localhost:8080/api/v1/osm/trails/ingest/bbox?south=45.2&west=25.4&north=45.5&east=25.7"
```

### Export Trail as GeoJSON
```bash
# List all trails first
curl http://localhost:8080/api/v1/trails

# Export specific trail by UUID
curl http://localhost:8080/api/v1/osm/trails/{trail-uuid}/geojson > trail.geojson
```

### Export Trail as GPX
```bash
curl http://localhost:8080/api/v1/osm/trails/{trail-uuid}/gpx > trail.gpx
```

### Search Trails
```bash
# Search by name
curl "http://localhost:8080/api/v1/osm/trails/search?q=Omu"

# Get trails by source
curl http://localhost:8080/api/v1/osm/trails/source/openstreetmap

# Get trail by OSM ID
curl http://localhost:8080/api/v1/osm/trails/osm-id/12345678
```

---

## 📊 Database Queries

### Get All Trails with Details
```sql
SELECT
  id, name, distance, elevation_gain, difficulty, source
FROM trails
ORDER BY distance DESC;
```

### Get Trails by Difficulty
```sql
SELECT
  id, name, distance, elevation_gain, difficulty
FROM trails
WHERE difficulty = 'HARD'
ORDER BY elevation_gain DESC;
```

### Get Waypoints for a Trail
```sql
SELECT
  w.sequence_order, w.name, w.type, w.latitude, w.longitude, w.elevation
FROM trail_waypoints w
JOIN trails t ON w.trail_id = t.id
WHERE t.name LIKE '%Omu%'
ORDER BY w.sequence_order;
```

### Find Nearby Trails (PostGIS)
```sql
-- Find trails within 5km of Sinaia (45.348°N, 25.540°E)
SELECT
  id, name, difficulty, distance,
  ST_Distance(geometry, ST_Point(25.540, 45.348)::geography) / 1000 AS distance_km
FROM trails
WHERE ST_DWithin(geometry, ST_Point(25.540, 45.348)::geography, 5000)
ORDER BY distance_km;
```

### Get Trail Statistics
```sql
SELECT
  difficulty,
  COUNT(*) as count,
  ROUND(AVG(distance)::numeric, 2) as avg_distance,
  MAX(elevation_gain) as max_elevation,
  ROUND(AVG(duration_minutes)/60::numeric, 1) as avg_hours
FROM trails
GROUP BY difficulty
ORDER BY difficulty;
```

---

## 🗂️ File Structure

```
services/trail-service/src/main/java/com/trailequip/trail/
├── adapter/
│   ├── rest/
│   │   ├── TrailController.java (existing)
│   │   └── OSMTrailController.java (NEW)
│   └── dto/
│       ├── TrailDto.java (NEW)
│       ├── TrailMarkingDto.java (NEW)
│       ├── WaypointDto.java (NEW)
│       └── CreateTrailDto.java (NEW)
├── application/service/
│   ├── TrailApplicationService.java (existing)
│   ├── TrailNormalizer.java (NEW)
│   ├── OSMIngestionService.java (NEW)
│   └── TrailExportService.java (NEW)
├── domain/
│   ├── model/
│   │   ├── Trail.java (enhanced)
│   │   ├── TrailMarking.java (NEW)
│   │   ├── TrailSegment.java (NEW)
│   │   ├── Waypoint.java (enhanced)
│   │   ├── Difficulty.java (enhanced)
│   │   └── DifficultyClassifier.java (existing)
│   ├── repository/
│   │   └── TrailRepository.java (enhanced)
│   └── service/
│       └── (domain services)
└── infrastructure/
    └── overpass/
        ├── OverpassApiClient.java (NEW)
        └── OverpassRelation.java (NEW)
```

---

## 🔌 API Endpoints Reference

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/osm/trails/ingest/bucegi` | Ingest Bucegi trails |
| POST | `/api/v1/osm/trails/ingest/bbox` | Ingest by bounding box |
| POST | `/api/v1/osm/trails/ingest/{osmId}` | Ingest single trail |
| POST | `/api/v1/osm/trails/ingest/nearby` | Ingest nearby trails |
| GET | `/api/v1/osm/trails/{id}/geojson` | Export as GeoJSON |
| GET | `/api/v1/osm/trails/{id}/gpx` | Export as GPX |
| GET | `/api/v1/osm/trails/all/geojson` | Export all as GeoJSON |
| GET | `/api/v1/osm/trails/search` | Search trails |
| GET | `/api/v1/osm/trails/source/{source}` | Get by source |
| GET | `/api/v1/osm/trails/osm-id/{osmId}` | Get by OSM ID |
| GET | `/api/v1/osm/trails/health` | Health check |

---

## 🛠️ Configuration

### Overpass API Settings
In `application.properties`:
```properties
overpass.timeout=60000
overpass.rate-limit=3000
```

### Database PostGIS
PostgreSQL must have PostGIS extension:
```sql
CREATE EXTENSION IF NOT EXISTS postgis;
SELECT PostGIS_version();
```

---

## 🐛 Troubleshooting

### "PostGIS extension not available"
```bash
# Install PostGIS on your PostgreSQL instance
apt-get install postgresql-15-postgis-3
# or
brew install postgis  # on macOS
```

### "Overpass API timeout"
- Check internet connection
- Verify Overpass API status: https://overpass-api.de/api/status
- Increase timeout in configuration
- Try smaller bounding box

### "No trails imported"
```bash
# Check database connection
curl http://localhost:8080/api/v1/osm/trails/health

# Check logs for errors
docker logs trail-service

# Verify Overpass query manually
curl -X POST "https://overpass-api.de/api/interpreter" \
  --data '[bbox:45.2,25.4,45.5,25.7];(relation[type=route][route=hiking];);out geom;'
```

### "GeoJSON export not working"
- Verify trail has geometry: `SELECT ST_AsText(geometry) FROM trails WHERE id = '...'`
- Check trail has waypoints
- Review TrailExportService logs

---

## 📈 Performance Tips

### Database Optimization
```sql
-- Analyze query plans
EXPLAIN ANALYZE SELECT * FROM trails WHERE difficulty = 'HARD';

-- Reindex if necessary
REINDEX TABLE trails;
REINDEX TABLE trail_waypoints;
REINDEX TABLE trail_segments;
```

### API Rate Limiting
- Default: 3 seconds between Overpass API requests
- Adjust in OverpassApiClient.java if needed
- Respect Overpass API fair use guidelines

### Large Exports
- For >500 trails, use pagination
- Consider compression: gzip exported GeoJSON
- Use streaming for GPX export

---

## 📝 Implementation Checklist

Before deploying to production:

- [ ] Database created and PostGIS installed
- [ ] Spring Boot application running
- [ ] API health check passes
- [ ] Test Overpass API connection
- [ ] Ingest sample trails (small bbox first)
- [ ] Verify waypoints created correctly
- [ ] Test GeoJSON export in map application
- [ ] Test GPX export in GPS device
- [ ] Run database performance tests
- [ ] Configure ingestion scheduler (optional)
- [ ] Set up monitoring/alerts
- [ ] Document custom OSM queries for your region

---

## 🔗 Useful Links

- **Overpass API**: https://overpass-api.de
- **OSM Wiki - Hiking Routes**: https://wiki.openstreetmap.org/wiki/Tag:route%3Dhiking
- **OSMC Symbol Standard**: https://wiki.openstreetmap.org/wiki/Osmc:symbol
- **PostGIS Documentation**: https://postgis.net/docs/
- **GeoJSON Spec**: https://geojson.org/
- **GPX Schema**: https://www.topografix.com/GPX/1/1/
- **ODbL License**: https://opendatacommons.org/licenses/odbl/

---

## 💡 Pro Tips

1. **Use Overpass Turbo** for testing queries: https://overpass-turbo.osm.de
2. **Validate GPX files** online: https://www.geoplaner.com/
3. **View GeoJSON** in browser: https://geojson.io/
4. **Monitor Overpass** rate limits and plan ingestion accordingly
5. **Cache trail exports** for frequently accessed routes
6. **Use PostGIS spatial indexes** for large datasets

---

**Happy hiking! 🥾**
