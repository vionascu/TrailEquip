# Muntii Nostri Trail Integration Guide

This document describes how to add the "Sinaia - Vârful Omu - Refugiul Țigănești - Bușteni" trail from Muntii Nostri website to the TrailEquip project.

**Source:** https://muntii-nostri.ro/ro/routeinfo/traseu-1-2-zile-sinaia-varful-omu-refugiul-tiganesti-busteni

---

## Trail Information Summary

| Property | Value |
|----------|-------|
| **Name** | Sinaia - Vârful Omu - Refugiul Țigănești - Bușteni |
| **Route Type** | Multi-day alpine traverse |
| **Distance** | 40.98 km |
| **Duration** | 11:00-13:45 hours (825-825 minutes) |
| **Elevation Gain** | 2,020 m |
| **Elevation Loss** | 1,930 m |
| **Highest Point** | 2,507 m (Vârful Omu) |
| **Difficulty** | HARD |
| **Best Season** | May - September |
| **Water Sources** | Very limited (primary at Piatra Arsă Cabin) |

---

## Trail Route Stages

The trail follows this sequence of waypoints:

1. **Sinaia** (Starting Point)
2. **Cabana Piatra Arsă** (Main water source)
3. **Cabana Babele (Sfinxul)**
4. **Vârful Caraiman** (Peak)
5. **Vârful Omu** (2,507 m - Highest point)
6. **Vârful Scara** (Peak)
7. **Refugiul Țigănești** (Mountain refuge - overnight)
8. **Cabana Mălăieștii** (Cabin)
9. **Poiana Pichetul Roșu** (Red Picket Meadow)
10. **Bușteni** (End Point)

---

## Trail Terrain Types

- Forest
- Alpine meadows
- Exposed ridges
- Scrambling sections

---

## Identified Hazards

- **Exposure:** Significant sections on exposed ridges
- **Bears:** Multiple bear encounter reports along the route
- **Limited Water Sources:** Very few water access points; primary source at Piatra Arsă Cabin
- **Weather Dependent:** Alpine weather changes quickly; suitable only May-September

---

## Database Integration

The trail has been added to the database seed data in `/infra/db/init.sql`:

```sql
INSERT INTO trails (id, name, description, distance, elevation_gain, elevation_loss,
  duration_minutes, max_slope, avg_slope, terrain, difficulty, hazards, source,
  created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440004'::uuid,
  'Sinaia - Vârful Omu - Refugiul Țigănești - Bușteni',
  'Multi-day alpine traverse featuring Bucegi peaks, scenic ridgelines, and mountain refuges.
   Route traverses from Sinaia through Piatra Arsă Cabin to Vârful Omu (2507m), Vârful Scara,
   continuing to Refugiul Țigănești and descending to Bușteni via alpine meadows.
   Crosses exposed alpine terrain with panoramic views.',
  40.98, 2020, 1930, 825, 45.0, 18.5,
  ARRAY['forest', 'alpine_meadow', 'exposed_ridge', 'scramble'],
  'HARD',
  ARRAY['exposure', 'bears', 'limited_water_sources', 'weather_dependent'],
  'muntii-nostri.ro',
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

---

## API Endpoint: Create Trail with Waypoints

To add this trail via REST API with full waypoint annotations:

**HTTP Request:**
```
POST /api/v1/trails
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Sinaia - Vârful Omu - Refugiul Țigănești - Bușteni",
  "description": "Multi-day alpine traverse featuring Bucegi peaks, scenic ridgelines, and mountain refuges. Route traverses from Sinaia through Piatra Arsă Cabin to Vârful Omu (2507m), Vârful Scara, continuing to Refugiul Țigănești and descending to Bușteni via alpine meadows. Crosses exposed alpine terrain with panoramic views.",
  "distance": 40.98,
  "elevationGain": 2020,
  "elevationLoss": 1930,
  "durationMinutes": 825,
  "maxSlope": 45.0,
  "avgSlope": 18.5,
  "terrain": ["forest", "alpine_meadow", "exposed_ridge", "scramble"],
  "difficulty": "HARD",
  "hazards": ["exposure", "bears", "limited_water_sources", "weather_dependent"],
  "waypoints": [
    {
      "name": "Sinaia",
      "description": "Starting point - town at base of trail",
      "latitude": 45.3514,
      "longitude": 25.5197,
      "elevation": 950
    },
    {
      "name": "Cabana Piatra Arsă",
      "description": "First major waypoint - primary water source along the route",
      "latitude": 45.3641,
      "longitude": 25.5089,
      "elevation": 1530
    },
    {
      "name": "Cabana Babele (Sfinxul)",
      "description": "Mountain cabin near the Sphinx rock formation",
      "latitude": 45.3858,
      "longitude": 25.5025,
      "elevation": 2000
    },
    {
      "name": "Vârful Caraiman",
      "description": "Peak along the ridge",
      "latitude": 45.3925,
      "longitude": 25.5142,
      "elevation": 2384
    },
    {
      "name": "Vârful Omu",
      "description": "Highest point on the trail at 2507m - panoramic views",
      "latitude": 45.3967,
      "longitude": 25.5264,
      "elevation": 2507
    },
    {
      "name": "Vârful Scara",
      "description": "Peak following Omu - exposed ridge section",
      "latitude": 45.3892,
      "longitude": 25.5472,
      "elevation": 2340
    },
    {
      "name": "Refugiul Țigănești",
      "description": "Mountain refuge - overnight accommodation point",
      "latitude": 45.3758,
      "longitude": 25.5614,
      "elevation": 1860
    },
    {
      "name": "Cabana Mălăieștii",
      "description": "Mountain cabin on descent route",
      "latitude": 45.3625,
      "longitude": 25.5719,
      "elevation": 1520
    },
    {
      "name": "Poiana Pichetul Roșu",
      "description": "Red Picket Meadow - scenic alpine meadow area",
      "latitude": 45.3528,
      "longitude": 25.5831,
      "elevation": 1350
    },
    {
      "name": "Bușteni",
      "description": "Ending point - town at base of trail",
      "latitude": 45.3412,
      "longitude": 25.5963,
      "elevation": 850
    }
  ],
  "source": "muntii-nostri.ro"
}
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440004",
  "name": "Sinaia - Vârful Omu - Refugiul Țigănești - Bușteni",
  "description": "Multi-day alpine traverse...",
  "distance": 40.98,
  "elevationGain": 2020,
  "elevationLoss": 1930,
  "durationMinutes": 825,
  "maxSlope": 45.0,
  "avgSlope": 18.5,
  "terrain": ["forest", "alpine_meadow", "exposed_ridge", "scramble"],
  "difficulty": "HARD",
  "hazards": ["exposure", "bears", "limited_water_sources", "weather_dependent"],
  "waypoints": [
    {
      "name": "Sinaia",
      "latitude": 45.3514,
      "longitude": 25.5197,
      "elevation": 950
    },
    ...
  ],
  "source": "muntii-nostri.ro",
  "createdAt": "2026-01-30T...",
  "updatedAt": "2026-01-30T..."
}
```

---

## Testing

### Via cURL

```bash
# Add the trail via API
curl -X POST http://localhost:8080/api/v1/trails \
  -H "Content-Type: application/json" \
  -d @- << 'EOF'
{
  "name": "Sinaia - Vârful Omu - Refugiul Țigănești - Bușteni",
  "description": "Multi-day alpine traverse...",
  "distance": 40.98,
  "elevationGain": 2020,
  "elevationLoss": 1930,
  "durationMinutes": 825,
  "maxSlope": 45.0,
  "avgSlope": 18.5,
  "terrain": ["forest", "alpine_meadow", "exposed_ridge", "scramble"],
  "difficulty": "HARD",
  "hazards": ["exposure", "bears", "limited_water_sources", "weather_dependent"],
  "source": "muntii-nostri.ro"
}
EOF

# Retrieve all trails
curl -X GET http://localhost:8080/api/v1/trails

# Filter by difficulty
curl -X GET http://localhost:8080/api/v1/trails?difficulty=HARD

# Get specific trail
curl -X GET http://localhost:8080/api/v1/trails/550e8400-e29b-41d4-a716-446655440004
```

### Running the Application

```bash
# Start all services via Docker Compose
cd /Users/viionascu/Projects/TrailEquip
docker-compose up

# Or run single terminal startup
./run-all.sh
```

---

## Implementation Notes

### Trail Characteristics

- **Multi-day Trek:** 11-14 hours of hiking spread across 1-2 days
- **High Altitude:** Summit at 2,507 m requires acclimatization
- **Alpine Terrain:** Exposed ridges and scrambling sections
- **Wildlife:** Bear encounters are a realistic hazard
- **Resource Constraint:** Limited water availability requires planning

### Seasonal Considerations

- **Optimal Season:** May-September (snow-free, stable weather)
- **Winter:** Trail is avalanche-prone and difficult; not recommended
- **Spring/Fall:** Weather is unpredictable; early starts essential
- **Summer:** Best conditions but can be crowded

### Safety Recommendations

1. **Water Management:** Carry extra water or plan stops at known sources
2. **Bear Safety:** Make noise while hiking, store food properly
3. **Weather:** Check forecasts; alpine weather changes rapidly
4. **Permits:** Check local requirements before departure
5. **Fitness:** This is a challenging multi-day trek requiring good fitness

---

## Related Documentation

- [Trail Service Tests](./automated-tests/rest-tests/TRAIL_SERVICE_TESTS.md)
- [Trail Model](./services/trail-service/src/main/java/com/trailequip/trail/domain/model/Trail.java)
- [Database Schema](./infra/db/init.sql)
- [Source Website](https://muntii-nostri.ro/ro/routeinfo/traseu-1-2-zile-sinaia-varful-omu-refugiul-tiganesti-busteni)

---

**Last Updated:** January 30, 2026
**Integration Status:** Database seed data added ✓ | API documentation provided ✓
