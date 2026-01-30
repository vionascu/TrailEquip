# TrailEquip: OpenStreetMap Integration Architecture

## Executive Summary

TrailEquip is a production-grade hiking trail application that uses **OpenStreetMap (OSM) as the authoritative data source** for trail geometry, trail markings, and metadata. This ensures:
- ✅ Free, open data (ODbL compliant)
- ✅ Official trail markings (OSMC standards)
- ✅ Crowdsourced accuracy and updates
- ✅ Real hiking data used in the field

---

## 1. DATA SOURCE: OVERPASS API

### 1.1 Overpass QL Query for Bucegi Mountains Trails

The primary data extraction uses **Overpass API** to fetch hiking routes from OSM:

```ql
[bbox:45.2,25.4,45.5,25.7];
(
  relation[type=route][route=hiking];
  relation[type=route][route=foot];
  relation[type=route][route=alpine_hiking];
);
out geom;
```

**Explanation:**
- `[bbox:45.2,25.4,45.5,25.7]` - Bounding box for Bucegi Mountains (lat_min, lon_min, lat_max, lon_max)
- `relation[type=route][route=hiking]` - Fetch hiking route relations
- `out geom` - Include full geometry (ways + nodes)

### 1.2 Enhanced Query with Trail Marking Data

```ql
[bbox:45.2,25.4,45.5,25.7];
(
  relation[type=route][route=hiking];
  relation[type=route][route=foot];
  relation[type=route][route=alpine_hiking];
);
out geom;

// Also fetch relation metadata
[bbox:45.2,25.4,45.5,25.7];
(
  relation[type=route][route=hiking];
  relation[type=route][route=foot];
  relation[type=route][route=alpine_hiking];
);
out body;
```

### 1.3 Query Data Returned

Each hiking route relation includes:

```
relation (osm_id)
  tags:
    name = "Sinaia - Vârful Omu - Refugiul Țigănești - Bușteni"
    ref = "01MN02"  // Muntii Nostri reference
    colour = "blue"
    osmc:symbol = "blue:blue_stripe"
    difficulty = "intermediate"
    description = "Multi-day alpine trek"
    duration = "11:00"
    distance = "40.98"

  members:
    way[ref] - trail segments
    node[ref] - waypoints (with lat/lon)
```

### 1.4 Bucegi Bounding Box

```
North: 45.50 (Piatra Mare ridge)
South: 45.20 (Brașov area)
East:  25.70 (Eastern Bucegi)
West:  25.40 (Western Bucegi)
```

---

## 2. DATA MODEL: JAVA DOMAIN CLASSES

### 2.1 Core Trail Entity

```java
package com.trailequip.trail.domain.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.LineString;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trails")
public class Trail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // OSM Metadata
    @Column(unique = true, nullable = false)
    private Long osmId;  // OpenStreetMap relation ID

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String ref;  // Reference: "01MN02", "02MN06", etc.

    // Trail Geometry
    @Column(columnDefinition = "Geometry(LineString,4326)")
    private LineString geometry;  // PostgreSQL PostGIS storage

    // Trail Statistics
    @Column(nullable = false)
    private Double distance;  // in km

    private Integer elevationGain;  // in meters
    private Integer elevationLoss;   // in meters
    private Integer durationMinutes;
    private Double maxSlope;  // in degrees
    private Double avgSlope;  // in degrees
    private Integer maxElevation;  // in meters

    // Trail Classification
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;  // EASY, MODERATE, HARD, ALPINE, SCRAMBLING

    @ElementCollection
    @CollectionTable(name = "trail_terrain")
    private List<String> terrain;  // forest, alpine_meadow, rock, exposed_ridge

    @ElementCollection
    @CollectionTable(name = "trail_hazards")
    private List<String> hazards;  // exposure, bears, water_crossings, loose_rock

    // Trail Marking (OSMC Standard)
    @OneToOne(cascade = CascadeType.ALL)
    private TrailMarking marking;  // Color, symbol, stripe type

    // Trail Waypoints
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "trail_id")
    private List<Waypoint> waypoints;

    // Trail Segments (for incremental updates)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "trail_id")
    private List<TrailSegment> segments;

    // Metadata
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(length = 100)
    private String source;  // "openstreetmap", "muntii-nostri", "wikiloc"

    // ===== CONSTRUCTORS & GETTERS =====

    public Trail() {}

    public Trail(Long osmId, String name, String ref, Double distance,
                 Difficulty difficulty, TrailMarking marking) {
        this.osmId = osmId;
        this.name = name;
        this.ref = ref;
        this.distance = distance;
        this.difficulty = difficulty;
        this.marking = marking;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters omitted for brevity
    public UUID getId() { return id; }
    public Long getOsmId() { return osmId; }
    public String getName() { return name; }
    public String getRef() { return ref; }
    public LineString getGeometry() { return geometry; }
    public TrailMarking getMarking() { return marking; }
    public List<Waypoint> getWaypoints() { return waypoints; }
    public List<TrailSegment> getSegments() { return segments; }
    public Difficulty getDifficulty() { return difficulty; }

    // Setters
    public void setGeometry(LineString geometry) { this.geometry = geometry; }
    public void setWaypoints(List<Waypoint> waypoints) { this.waypoints = waypoints; }
    public void setSegments(List<TrailSegment> segments) { this.segments = segments; }
}
```

### 2.2 Trail Marking (OSMC Symbol Standard)

```java
package com.trailequip.trail.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "trail_markings")
public class TrailMarking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OSMC Symbol: Format = "background:foreground_symbol"
    // Example: "blue:blue_stripe", "red:red_triangle", "yellow:yellow_cross"
    @Column(nullable = false, unique = true, length = 100)
    private String osmc_symbol;  // Full OSMC symbol string

    @Enumerated(EnumType.STRING)
    private MarkingColor color;  // BLUE, RED, YELLOW, GREEN, WHITE, ORANGE

    @Enumerated(EnumType.STRING)
    private MarkingShape shape;  // STRIPE, TRIANGLE, CROSS, DOT, RECTANGLE

    @Column(length = 20)
    private String hexColor;  // e.g., "#0000FF" for blue

    @Column(columnDefinition = "TEXT")
    private String description;  // "Blue striped main trail", etc.

    // ===== ENUMS =====

    public enum MarkingColor {
        BLUE("#0000FF"),
        RED("#FF0000"),
        YELLOW("#FFFF00"),
        GREEN("#00AA00"),
        WHITE("#FFFFFF"),
        ORANGE("#FFA500"),
        BLACK("#000000"),
        PURPLE("#800080");

        private final String hex;

        MarkingColor(String hex) { this.hex = hex; }
        public String getHex() { return hex; }
    }

    public enum MarkingShape {
        STRIPE("━"),      // Horizontal stripe
        TRIANGLE("▲"),    // Triangle/pyramid
        CROSS("✛"),       // Plus sign cross
        DOT("●"),         // Circle dot
        RECTANGLE("■"),   // Square rectangle
        ARCH("⌢"),        // Arc
        NONE("");         // No symbol

        private final String symbol;

        MarkingShape(String symbol) { this.symbol = symbol; }
        public String getSymbol() { return symbol; }
    }

    // ===== CONSTRUCTORS & GETTERS =====

    public TrailMarking() {}

    public TrailMarking(String osmc_symbol, MarkingColor color, MarkingShape shape) {
        this.osmc_symbol = osmc_symbol;
        this.color = color;
        this.shape = shape;
        this.hexColor = color.getHex();
        this.description = color + " " + shape;
    }

    public String getOsmcSymbol() { return osmc_symbol; }
    public MarkingColor getColor() { return color; }
    public MarkingShape getShape() { return shape; }
    public String getHexColor() { return hexColor; }
    public String getDescription() { return description; }
}
```

### 2.3 Trail Segment (For Detailed Path)

```java
package com.trailequip.trail.domain.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.LineString;

@Entity
@Table(name = "trail_segments")
public class TrailSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Trail trail;

    @Column(nullable = false)
    private Long osmWayId;  // OpenStreetMap way ID

    @Column(nullable = false)
    private Integer sequenceOrder;  // Order in trail

    @Column(columnDefinition = "Geometry(LineString,4326)")
    private LineString geometry;  // Actual trail path

    @Column(nullable = false)
    private Double length;  // in km

    @Enumerated(EnumType.STRING)
    private TerrainType terrainType;

    @Column(nullable = false)
    private Boolean accessible;  // Can this segment be hiked?

    @Column(columnDefinition = "TEXT")
    private String notes;  // "Steep scramble", "Water crossing", etc.

    // ===== ENUM =====

    public enum TerrainType {
        FOREST("Dense tree coverage"),
        ALPINE_MEADOW("High altitude grassland"),
        ROCK("Exposed rock/scramble"),
        EXPOSED_RIDGE("Windy, exposed height"),
        WATER_CROSSING("Stream/river crossing"),
        LOOSE_ROCK("Unstable terrain"),
        PAVED("Road/pavement"),
        SCREE("Loose rock slope");

        private final String description;

        TerrainType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    // ===== GETTERS =====

    public Long getId() { return id; }
    public Trail getTrail() { return trail; }
    public Long getOsmWayId() { return osmWayId; }
    public Integer getSequenceOrder() { return sequenceOrder; }
    public LineString getGeometry() { return geometry; }
    public Double getLength() { return length; }
    public TerrainType getTerrainType() { return terrainType; }
    public Boolean isAccessible() { return accessible; }
    public String getNotes() { return notes; }
}
```

### 2.4 Waypoint (Route Stop)

```java
package com.trailequip.trail.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "trail_waypoints")
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Trail trail;

    @Column(nullable = false)
    private Long osmNodeId;  // OpenStreetMap node ID

    @Column(nullable = false)
    private Integer sequenceOrder;  // Order along trail

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer elevation;  // in meters

    @Column(nullable = false)
    private String name;  // "Cabana Piatra Arsă", "Vârful Omu", etc.

    @Enumerated(EnumType.STRING)
    private WaypointType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ===== ENUM =====

    public enum WaypointType {
        SHELTER("Mountain refuge/cabin"),
        PEAK("Mountain summit"),
        WATER("Water source"),
        JUNCTION("Trail junction"),
        START("Trail start"),
        END("Trail end"),
        CAMPING("Camping area"),
        VIEWPOINT("Scenic viewpoint"),
        OTHER("Other point of interest");

        private final String description;

        WaypointType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    // ===== GETTERS =====

    public Long getId() { return id; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Integer getElevation() { return elevation; }
    public String getName() { return name; }
    public WaypointType getType() { return type; }
    public String getDescription() { return description; }
    public Integer getSequenceOrder() { return sequenceOrder; }
}
```

### 2.5 Difficulty Enum

```java
package com.trailequip.trail.domain.model;

public enum Difficulty {
    EASY(
        "Easy - Minimal elevation, well-maintained paths",
        maxSlope = 10.0,
        minTechLevel = 0
    ),
    MODERATE(
        "Moderate - Some elevation, occasional rocky sections",
        maxSlope = 20.0,
        minTechLevel = 1
    ),
    HARD(
        "Hard - Significant elevation, exposed terrain",
        maxSlope = 30.0,
        minTechLevel = 2
    ),
    ALPINE(
        "Alpine - High altitude, thin air, exposed ridges",
        maxSlope = 40.0,
        minTechLevel = 3
    ),
    SCRAMBLING(
        "Scrambling - Hands required, technical terrain",
        maxSlope = 50.0,
        minTechLevel = 4
    );

    private final String description;
    private final Double maxSlope;
    private final Integer minTechLevel;

    Difficulty(String description, Double maxSlope, Integer minTechLevel) {
        this.description = description;
        this.maxSlope = maxSlope;
        this.minTechLevel = minTechLevel;
    }

    public String getDescription() { return description; }
    public Double getMaxSlope() { return maxSlope; }
    public Integer getMinTechLevel() { return minTechLevel; }
}
```

---

## 3. BACKEND ARCHITECTURE

### 3.1 Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    REST API LAYER                       │
│  GET /api/v1/trails                                     │
│  GET /api/v1/trails/{id}/geojson                        │
│  GET /api/v1/trails/{id}/gpx                            │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              APPLICATION LAYER                          │
│  TrailApplicationService                                │
│  - Trail queries                                        │
│  - Trail filtering                                      │
│  - GeoJSON conversion                                   │
│  - GPX generation                                       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              DOMAIN LAYER                               │
│  Trail, TrailMarking, Waypoint, TrailSegment            │
│  Repository interfaces                                  │
│  DomainServices (DifficultyClassifier, etc.)            │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│          INFRASTRUCTURE LAYER                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  PostgreSQL + PostGIS                            │   │
│  │  trails (geometry, marking, segments)            │   │
│  │  trail_waypoints (lat/lon, elevation, type)      │   │
│  │  trail_segments (detailed path)                  │   │
│  └──────────────────────────────────────────────────┘   │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  OSM Ingestion Service                           │   │
│  │  - Overpass API client                           │   │
│  │  - Trail parser                                  │   │
│  │  - Duplicate detection                           │   │
│  │  - Quality validation                            │   │
│  └──────────────────────────────────────────────────┘   │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  External Services                               │   │
│  │  - Overpass API (OSM data)                        │   │
│  │  - Elevation service (optional)                  │   │
│  │  - Cache layer (Redis optional)                  │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 3.2 OSM Ingestion Service

```java
package com.trailequip.trail.infrastructure.osm;

import org.springframework.stereotype.Service;
import com.trailequip.trail.domain.model.*;
import com.trailequip.trail.domain.repository.TrailRepository;
import java.util.List;

@Service
public class OSMIngestionService {

    private final OverpassApiClient overpassClient;
    private final TrailRepository trailRepository;
    private final TrailNormalizer normalizer;

    /**
     * Fetch and ingest trails from OSM Overpass API for Bucegi Mountains
     */
    public void ingestBucegiTrails() {
        // 1. Query Overpass API
        String overpassQuery = """
            [bbox:45.2,25.4,45.5,25.7];
            (
              relation[type=route][route=hiking];
              relation[type=route][route=foot];
              relation[type=route][route=alpine_hiking];
            );
            out geom;
        """;

        List<OverpassRelation> relations = overpassClient.queryRelations(overpassQuery);

        // 2. Normalize and parse each relation
        for (OverpassRelation relation : relations) {
            Trail trail = normalizer.fromOSMRelation(relation);

            // 3. Validate trail data
            if (isValidTrail(trail)) {
                // 4. Check for duplicates (by osmId)
                if (!trailRepository.existsByOsmId(trail.getOsmId())) {
                    // 5. Save to database
                    trailRepository.save(trail);
                    System.out.println("Ingested trail: " + trail.getName());
                } else {
                    System.out.println("Trail already exists: " + trail.getOsmId());
                }
            }
        }
    }

    private boolean isValidTrail(Trail trail) {
        return trail.getName() != null &&
               trail.getGeometry() != null &&
               trail.getDistance() > 0 &&
               trail.getWaypoints().size() >= 2;
    }
}
```

### 3.3 Trail Normalizer (OSM to Domain)

```java
package com.trailequip.trail.infrastructure.osm;

import com.trailequip.trail.domain.model.*;
import java.util.*;

@Service
public class TrailNormalizer {

    /**
     * Convert OpenStreetMap relation to Trail domain model
     */
    public Trail fromOSMRelation(OverpassRelation osmRelation) {
        // Extract metadata
        String name = osmRelation.getTags().get("name");
        Long osmId = osmRelation.getId();
        String ref = osmRelation.getTags().get("ref");  // e.g., "01MN02"

        Trail trail = new Trail(osmId, name, ref, calculateDistance(osmRelation),
                                deriveDifficulty(osmRelation),
                                parseMarking(osmRelation));

        // Parse marking
        String osmc_symbol = osmRelation.getTags().get("osmc:symbol");
        String colour = osmRelation.getTags().get("colour");
        TrailMarking marking = parseOSMCSymbol(osmc_symbol, colour);
        trail.setMarking(marking);

        // Parse waypoints from relation members
        List<Waypoint> waypoints = parseWaypoints(osmRelation.getMembers());
        trail.setWaypoints(waypoints);

        // Parse segments
        List<TrailSegment> segments = parseSegments(osmRelation.getWays());
        trail.setSegments(segments);

        // Extract other metadata
        trail.setDescription(osmRelation.getTags().get("description"));
        trail.setElevationGain(parseInteger(osmRelation.getTags().get("elevation_gain")));
        trail.setElevationLoss(parseInteger(osmRelation.getTags().get("elevation_loss")));
        trail.setDurationMinutes(parseDuration(osmRelation.getTags().get("duration")));
        trail.setSource("openstreetmap");

        return trail;
    }

    /**
     * Parse OSMC symbol: "blue:blue_stripe" → TrailMarking
     */
    private TrailMarking parseOSMCSymbol(String osmc_symbol, String colour) {
        if (osmc_symbol == null || osmc_symbol.isEmpty()) {
            osmc_symbol = colour + ":unknown";  // Fallback
        }

        String[] parts = osmc_symbol.split(":");
        String background = parts.length > 0 ? parts[0] : colour;
        String foreground = parts.length > 1 ? parts[1] : "unknown";

        // Map color
        TrailMarking.MarkingColor color = mapOSMColorToEnum(background);

        // Map shape
        TrailMarking.MarkingShape shape = mapOSMShapeToEnum(foreground);

        return new TrailMarking(osmc_symbol, color, shape);
    }

    private TrailMarking.MarkingColor mapOSMColorToEnum(String osmColor) {
        return switch(osmColor.toLowerCase()) {
            case "blue" -> TrailMarking.MarkingColor.BLUE;
            case "red" -> TrailMarking.MarkingColor.RED;
            case "yellow" -> TrailMarking.MarkingColor.YELLOW;
            case "green" -> TrailMarking.MarkingColor.GREEN;
            case "white" -> TrailMarking.MarkingColor.WHITE;
            case "orange" -> TrailMarking.MarkingColor.ORANGE;
            case "purple", "violet" -> TrailMarking.MarkingColor.PURPLE;
            default -> TrailMarking.MarkingColor.BLACK;
        };
    }

    private TrailMarking.MarkingShape mapOSMShapeToEnum(String osmShape) {
        return switch(osmShape.toLowerCase()) {
            case "stripe", "bar" -> TrailMarking.MarkingShape.STRIPE;
            case "triangle" -> TrailMarking.MarkingShape.TRIANGLE;
            case "cross", "x" -> TrailMarking.MarkingShape.CROSS;
            case "dot", "circle" -> TrailMarking.MarkingShape.DOT;
            case "rectangle", "square" -> TrailMarking.MarkingShape.RECTANGLE;
            default -> TrailMarking.MarkingShape.NONE;
        };
    }

    private Difficulty deriveDifficulty(OverpassRelation osmRelation) {
        String difficulty = osmRelation.getTags().get("difficulty");
        if (difficulty != null) {
            return switch(difficulty.toLowerCase()) {
                case "easy" -> Difficulty.EASY;
                case "moderate", "intermediate" -> Difficulty.MODERATE;
                case "hard" -> Difficulty.HARD;
                case "alpine" -> Difficulty.ALPINE;
                case "scrambling" -> Difficulty.SCRAMBLING;
                default -> Difficulty.MODERATE;
            };
        }

        // Fallback: infer from elevation gain
        Integer elevGain = parseInteger(osmRelation.getTags().get("elevation_gain"));
        if (elevGain != null) {
            if (elevGain < 500) return Difficulty.EASY;
            if (elevGain < 1000) return Difficulty.MODERATE;
            if (elevGain < 1500) return Difficulty.HARD;
            return Difficulty.ALPINE;
        }

        return Difficulty.MODERATE;
    }

    private List<Waypoint> parseWaypoints(List<OverpassNode> nodes) {
        List<Waypoint> waypoints = new ArrayList<>();
        int order = 0;

        for (OverpassNode node : nodes) {
            Waypoint wp = new Waypoint();
            wp.setOsmNodeId(node.getId());
            wp.setLatitude(node.getLat());
            wp.setLongitude(node.getLon());
            wp.setSequenceOrder(order++);
            wp.setName(node.getTags().getOrDefault("name", "Waypoint " + order));
            wp.setElevation(parseInteger(node.getTags().get("ele")));
            wp.setType(inferWaypointType(node.getTags()));
            waypoints.add(wp);
        }

        return waypoints;
    }

    private Waypoint.WaypointType inferWaypointType(Map<String, String> tags) {
        if (tags.containsKey("tourism")) {
            String tourism = tags.get("tourism");
            return switch(tourism) {
                case "alpine_hut", "mountain_hut", "shelter" -> Waypoint.WaypointType.SHELTER;
                case "viewpoint" -> Waypoint.WaypointType.VIEWPOINT;
                case "camp_site" -> Waypoint.WaypointType.CAMPING;
                default -> Waypoint.WaypointType.OTHER;
            };
        }

        if (tags.containsKey("natural") && tags.get("natural").equals("peak")) {
            return Waypoint.WaypointType.PEAK;
        }

        if (tags.containsKey("man_made") && tags.get("man_made").equals("water_tap")) {
            return Waypoint.WaypointType.WATER;
        }

        return Waypoint.WaypointType.OTHER;
    }

    private Double calculateDistance(OverpassRelation relation) {
        // Calculate distance from geometry coordinates
        // Implementation uses Haversine formula
        return 0.0;  // Placeholder
    }

    private Integer parseInteger(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseDuration(String duration) {
        // Parse "11:00" or "11:00-13:45" format
        if (duration == null) return null;
        String[] parts = duration.split("-")[0].split(":");
        if (parts.length == 2) {
            int hours = Integer.parseInt(parts[0]);
            int mins = Integer.parseInt(parts[1]);
            return hours * 60 + mins;
        }
        return null;
    }
}
```

---

## 4. REST API ENDPOINTS

### 4.1 Trail Listing and Details

```java
@RestController
@RequestMapping("/api/v1/trails")
@Tag(name = "Trails", description = "Hiking trails from OpenStreetMap")
public class TrailController {

    private final TrailApplicationService trailService;
    private final GeoJSONConverter geoJsonConverter;

    /**
     * GET /api/v1/trails
     * Returns all trails with basic info
     */
    @GetMapping
    public ResponseEntity<List<TrailDto>> getAllTrails(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String marking_color) {

        List<Trail> trails = difficulty != null ?
            trailService.getTrailsByDifficulty(difficulty) :
            trailService.getAllTrails();

        if (marking_color != null) {
            trails = trails.stream()
                .filter(t -> t.getMarking().getColor().toString().equalsIgnoreCase(marking_color))
                .collect(Collectors.toList());
        }

        return ResponseEntity.ok(TrailDto.fromTrails(trails));
    }

    /**
     * GET /api/v1/trails/{id}
     * Returns full trail details including geometry and waypoints
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrailDetailDto> getTrailById(@PathVariable UUID id) {
        return trailService.getTrail(id)
            .map(trail -> ResponseEntity.ok(TrailDetailDto.fromTrail(trail)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/trails/{id}/geojson
     * Returns trail geometry as GeoJSON for Leaflet/Mapbox
     */
    @GetMapping("/{id}/geojson")
    public ResponseEntity<String> getTrailGeoJSON(@PathVariable UUID id) {
        return trailService.getTrail(id)
            .map(trail -> {
                String geoJson = geoJsonConverter.toFeatureCollection(trail);
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(geoJson);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/trails/{id}/gpx
     * Returns trail as GPX format for GPS devices
     */
    @GetMapping("/{id}/gpx")
    public ResponseEntity<String> getTrailGPX(@PathVariable UUID id) {
        return trailService.getTrail(id)
            .map(trail -> {
                String gpx = geoJsonConverter.toGPX(trail);
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .header("Content-Disposition", "attachment; filename=trail.gpx")
                    .body(gpx);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/trails/search/by-marking
     * Find trails by marking color/shape
     */
    @GetMapping("/search/by-marking")
    public ResponseEntity<List<TrailDto>> getTrailsByMarking(
            @RequestParam String color,
            @RequestParam(required = false) String shape) {

        List<Trail> trails = trailService.getTrailsByMarking(color, shape);
        return ResponseEntity.ok(TrailDto.fromTrails(trails));
    }

    /**
     * GET /api/v1/trails/search/by-location
     * Find trails near coordinates (geographic search)
     */
    @GetMapping("/search/by-location")
    public ResponseEntity<List<TrailDto>> getTrailsNearLocation(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {

        List<Trail> trails = trailService.getTrailsNearLocation(latitude, longitude, radiusKm);
        return ResponseEntity.ok(TrailDto.fromTrails(trails));
    }
}
```

### 4.2 Data Transfer Objects

```java
// TrailDto.java - For list views
@Data
@Builder
public class TrailDto {
    private UUID id;
    private Long osmId;
    private String name;
    private String ref;
    private Double distance;
    private Integer elevationGain;
    private String difficulty;
    private MarkingDto marking;
    private Integer waypointCount;

    @Data
    @Builder
    public static class MarkingDto {
        private String osmc_symbol;
        private String color;
        private String shape;
        private String hexColor;
    }

    public static List<TrailDto> fromTrails(List<Trail> trails) {
        return trails.stream()
            .map(TrailDto::fromTrail)
            .collect(Collectors.toList());
    }

    public static TrailDto fromTrail(Trail trail) {
        return TrailDto.builder()
            .id(trail.getId())
            .osmId(trail.getOsmId())
            .name(trail.getName())
            .ref(trail.getRef())
            .distance(trail.getDistance())
            .elevationGain(trail.getElevationGain())
            .difficulty(trail.getDifficulty().toString())
            .marking(MarkingDto.builder()
                .osmc_symbol(trail.getMarking().getOsmcSymbol())
                .color(trail.getMarking().getColor().toString())
                .shape(trail.getMarking().getShape().toString())
                .hexColor(trail.getMarking().getHexColor())
                .build())
            .waypointCount(trail.getWaypoints().size())
            .build();
    }
}

// TrailDetailDto.java - For detail views
@Data
@Builder
public class TrailDetailDto {
    private UUID id;
    private Long osmId;
    private String name;
    private String ref;
    private String description;
    private Double distance;
    private Integer elevationGain;
    private Integer elevationLoss;
    private Integer durationMinutes;
    private String difficulty;
    private List<String> terrain;
    private List<String> hazards;
    private MarkingDto marking;
    private List<WaypointDto> waypoints;
    private String geometryGeoJSON;

    // ... getters and builder
}

// WaypointDto.java
@Data
@Builder
public class WaypointDto {
    private Integer sequenceOrder;
    private String name;
    private String type;  // SHELTER, PEAK, WATER, etc.
    private Double latitude;
    private Double longitude;
    private Integer elevation;
    private String description;
}
```

---

## 5. FRONTEND INTEGRATION: TRAIL MARKING RENDERING

### 5.1 React Component: Trail Marker Rendering

```javascript
// TrailMarkerRenderer.tsx
import React from 'react';
import L from 'leaflet';

interface TrailMarking {
  osmc_symbol: string;
  color: string;
  shape: string;
  hexColor: string;
}

export class TrailMarkerRenderer {

  /**
   * Create Leaflet marker icon for trail marking
   */
  static createMarkerIcon(marking: TrailMarking, isStart: boolean = false) {
    const baseSize = 44;
    const borderColor = marking.hexColor;
    const symbol = this.getSymbolCharacter(marking.shape);

    const html = `
      <div style="
        width: ${baseSize}px;
        height: ${baseSize}px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: white;
        border: 4px solid ${borderColor};
        border-radius: 3px;
        box-shadow: 0 2px 6px rgba(0,0,0,0.4);
        font-weight: bold;
        font-size: 20px;
        color: ${borderColor};
        cursor: pointer;
      ">
        ${symbol}
      </div>
    `;

    return L.divIcon({
      className: 'trail-marker',
      html: html,
      iconSize: [baseSize, baseSize],
      iconAnchor: [baseSize / 2, baseSize / 2]
    });
  }

  /**
   * Draw trail polyline with proper marking visualization
   */
  static drawTrail(map: any, trail: Trail, marking: TrailMarking) {
    const markingColor = marking.hexColor || '#0000FF';

    // Draw white outline (for visibility)
    const outline = L.polyline(trail.waypoints, {
      color: '#FFFFFF',
      weight: 8,
      opacity: 1,
      lineCap: 'round',
      lineJoin: 'round'
    }).addTo(map);

    // Draw main trail line
    const mainLine = L.polyline(trail.waypoints, {
      color: markingColor,
      weight: 5,
      opacity: 1,
      lineCap: 'round',
      lineJoin: 'round',
      dashArray: this.getDashPattern(marking.shape)
    }).addTo(map);

    // Add marking symbols along the trail (every ~2km)
    const waypointSpacing = Math.max(1, Math.floor(trail.waypoints.length / 10));
    for (let i = 0; i < trail.waypoints.length; i += waypointSpacing) {
      const wp = trail.waypoints[i];
      const marker = L.marker(
        [wp.latitude, wp.longitude],
        { icon: this.createMarkerIcon(marking) }
      ).addTo(map);
    }
  }

  /**
   * Get dash pattern for line based on marking shape
   */
  private static getDashPattern(shape: string): string {
    switch (shape.toUpperCase()) {
      case 'STRIPE':
        return '';  // Solid line
      case 'TRIANGLE':
        return '5, 5';  // Dashed
      case 'CROSS':
        return '2, 2';  // Dotted
      case 'DOT':
        return '1, 3';  // Dot-dash
      default:
        return '';
    }
  }

  /**
   * Get Unicode symbol for marking shape
   */
  private static getSymbolCharacter(shape: string): string {
    switch (shape.toUpperCase()) {
      case 'STRIPE':
        return '━';
      case 'TRIANGLE':
        return '▲';
      case 'CROSS':
        return '✛';
      case 'DOT':
        return '●';
      case 'RECTANGLE':
        return '■';
      default:
        return '⊕';
    }
  }
}

// Usage in TrailMap component
export function TrailMap({ trails }: { trails: Trail[] }) {
  const mapRef = React.useRef<L.Map | null>(null);

  React.useEffect(() => {
    trails.forEach(trail => {
      if (trail.marking) {
        TrailMarkerRenderer.drawTrail(mapRef.current, trail, trail.marking);
      }
    });
  }, [trails]);

  return <div ref={mapContainerRef} style={{ height: '100vh' }} />;
}
```

### 5.2 Legend Component

```javascript
// TrailLegend.tsx
export function TrailLegend() {
  const markings = [
    { color: '#0000FF', shape: 'STRIPE', label: 'Blue Striped - Main Trail' },
    { color: '#FF0000', shape: 'TRIANGLE', label: 'Red Triangle - Summit Route' },
    { color: '#FFFF00', shape: 'CROSS', label: 'Yellow Cross - Junction' },
    { color: '#00AA00', shape: 'DOT', label: 'Green Dot - Branch' },
  ];

  return (
    <div className="legend">
      <h3>Trail Markings (OSMC Standard)</h3>
      {markings.map(m => (
        <div key={m.color + m.shape} className="legend-item">
          <div className="marking-icon" style={{
            borderColor: m.color,
            color: m.color
          }}>
            {TrailMarkerRenderer.getSymbolCharacter(m.shape)}
          </div>
          <span>{m.label}</span>
        </div>
      ))}
    </div>
  );
}
```

---

## 6. GPX EXPORT FUNCTIONALITY

### 6.1 GPX Generator Service

```java
package com.trailequip.trail.infrastructure.export;

import javax.xml.stream.*;
import java.io.StringWriter;

@Service
public class GPXExporter {

    /**
     * Convert Trail to GPX 1.1 format
     */
    public String toGPX(Trail trail) throws XMLStreamException {
        StringWriter sw = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(sw);

        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("gpx");
        writer.writeAttribute("version", "1.1");
        writer.writeAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
        writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        // Metadata
        writer.writeStartElement("metadata");
        writer.writeElement("name", trail.getName());
        writer.writeElement("desc", trail.getDescription());
        writer.writeElement("author", "OpenStreetMap Contributors");
        writer.writeElement("link", "https://www.openstreetmap.org/relation/" + trail.getOsmId());
        writer.writeElement("time", Instant.now().toString());
        writer.writeEndElement();  // metadata

        // Track
        writer.writeStartElement("trk");
        writer.writeElement("name", trail.getName());
        writer.writeElement("desc", buildTrailDescription(trail));
        writer.writeElement("type", "hiking");

        // Add trail marking as extension
        writer.writeStartElement("extensions");
        writer.writeElement("trail_marking", trail.getMarking().getOsmcSymbol());
        writer.writeElement("difficulty", trail.getDifficulty().toString());
        writer.writeElement("elevation_gain", trail.getElevationGain().toString());
        writer.writeEndElement();  // extensions

        // Track segment with waypoints
        writer.writeStartElement("trkseg");

        for (Waypoint wp : trail.getWaypoints()) {
            writer.writeStartElement("trkpt");
            writer.writeAttribute("lat", String.valueOf(wp.getLatitude()));
            writer.writeAttribute("lon", String.valueOf(wp.getLongitude()));
            writer.writeElement("ele", wp.getElevation().toString());
            writer.writeElement("name", wp.getName());
            writer.writeElement("desc", wp.getType().toString());
            writer.writeEndElement();  // trkpt
        }

        writer.writeEndElement();  // trkseg
        writer.writeEndElement();  // trk
        writer.writeEndElement();  // gpx
        writer.writeEndDocument();

        return sw.toString();
    }

    private String buildTrailDescription(Trail trail) {
        return String.format(
            "%s | Distance: %.2f km | Elevation: +%d m | Difficulty: %s | Marking: %s",
            trail.getDescription(),
            trail.getDistance(),
            trail.getElevationGain(),
            trail.getDifficulty(),
            trail.getMarking().getOsmcSymbol()
        );
    }
}
```

### 6.2 GeoJSON Converter

```java
package com.trailequip.trail.infrastructure.export;

import com.fasterxml.jackson.databind.node.*;

@Service
public class GeoJSONConverter {

    public String toFeatureCollection(Trail trail) {
        ObjectNode featureCollection = JsonNodeFactory.instance.objectNode();
        featureCollection.put("type", "FeatureCollection");

        ArrayNode features = JsonNodeFactory.instance.arrayNode();

        // Trail geometry feature
        ObjectNode trailFeature = createTrailFeature(trail);
        features.add(trailFeature);

        // Waypoint features
        for (Waypoint wp : trail.getWaypoints()) {
            ObjectNode wpFeature = createWaypointFeature(wp, trail);
            features.add(wpFeature);
        }

        featureCollection.set("features", features);
        return featureCollection.toString();
    }

    private ObjectNode createTrailFeature(Trail trail) {
        ObjectNode feature = JsonNodeFactory.instance.objectNode();
        feature.put("type", "Feature");

        ObjectNode properties = JsonNodeFactory.instance.objectNode();
        properties.put("name", trail.getName());
        properties.put("ref", trail.getRef());
        properties.put("distance", trail.getDistance());
        properties.put("difficulty", trail.getDifficulty().toString());
        properties.put("osmc_symbol", trail.getMarking().getOsmcSymbol());
        properties.put("color", trail.getMarking().getColor().toString());
        properties.put("shape", trail.getMarking().getShape().toString());
        feature.set("properties", properties);

        // Geometry (LineString)
        ObjectNode geometry = JsonNodeFactory.instance.objectNode();
        geometry.put("type", "LineString");
        ArrayNode coordinates = JsonNodeFactory.instance.arrayNode();
        for (Waypoint wp : trail.getWaypoints()) {
            ArrayNode coord = JsonNodeFactory.instance.arrayNode();
            coord.add(wp.getLongitude());
            coord.add(wp.getLatitude());
            coordinates.add(coord);
        }
        geometry.set("coordinates", coordinates);
        feature.set("geometry", geometry);

        return feature;
    }

    private ObjectNode createWaypointFeature(Waypoint wp, Trail trail) {
        ObjectNode feature = JsonNodeFactory.instance.objectNode();
        feature.put("type", "Feature");

        ObjectNode properties = JsonNodeFactory.instance.objectNode();
        properties.put("name", wp.getName());
        properties.put("type", wp.getType().toString());
        properties.put("elevation", wp.getElevation());
        properties.put("sequence", wp.getSequenceOrder());
        feature.set("properties", properties);

        // Geometry (Point)
        ObjectNode geometry = JsonNodeFactory.instance.objectNode();
        geometry.put("type", "Point");
        ArrayNode coordinates = JsonNodeFactory.instance.arrayNode();
        coordinates.add(wp.getLongitude());
        coordinates.add(wp.getLatitude());
        geometry.set("coordinates", coordinates);
        feature.set("geometry", geometry);

        return feature;
    }
}
```

---

## 7. LICENSING & ATTRIBUTION (ODbL)

### 7.1 Required Attribution

OpenStreetMap data is licensed under the **ODbL 1.0** (Open Data Commons Open Database License):

```
© OpenStreetMap contributors, ODbL 1.0
https://www.openstreetmap.org/copyright
```

**In your application:**

1. **Footer Attribution** (required):
```html
<div class="osm-attribution">
  © <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors
  | Data licensed under <a href="http://opendatacommons.org/licenses/odbl/">ODbL</a>
</div>
```

2. **On Map** (for Leaflet):
```javascript
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '© OpenStreetMap contributors, ODbL 1.0'
}).addTo(map);
```

3. **In Application Settings/About**:
```
Trail data sourced from OpenStreetMap
Licensed under Creative Commons Attribution-ShareAlike 2.0
© OpenStreetMap contributors
```

### 7.2 ODbL Compliance

- ✅ You can use OSM data commercially
- ✅ You can modify the data
- ⚠️ You must provide the attribution
- ⚠️ If you modify data, you must share modifications under ODbL
- ⚠️ Your database of trails becomes ODbL-licensed if derived from OSM

---

## 8. EDGE CASES & DATA QUALITY

### 8.1 Handling Missing OSMC Symbols

```java
@Service
public class TrailValidationService {

    /**
     * Validate trail data completeness
     */
    public ValidationResult validateTrail(Trail trail) {
        ValidationResult result = new ValidationResult();

        // Check required fields
        if (trail.getOsmId() == null) {
            result.addError("Missing OSM ID");
        }

        // Check marking
        if (trail.getMarking() == null ||
            trail.getMarking().getOsmcSymbol() == null) {
            result.addWarning("Missing or incomplete trail marking (osmc:symbol)");
            // Fallback: try to infer from tags
            inferMarkingFromContext(trail);
        }

        // Check geometry
        if (trail.getGeometry() == null || trail.getGeometry().isEmpty()) {
            result.addError("Missing trail geometry");
        }

        // Check waypoints
        if (trail.getWaypoints().size() < 2) {
            result.addError("Trail must have at least 2 waypoints");
        }

        // Check distance consistency
        double calculatedDistance = calculateDistanceFromGeometry(trail);
        if (Math.abs(calculatedDistance - trail.getDistance()) > 1.0) {
            result.addWarning("Distance mismatch: OSM says " + trail.getDistance() +
                            ", calculated " + calculatedDistance);
        }

        return result;
    }

    /**
     * Infer trail marking when OSMC symbol is missing
     */
    private void inferMarkingFromContext(Trail trail) {
        Map<String, String> tags = trail.getTags();

        // Try alternative OSM tags
        String colour = tags.get("colour");
        String ref = tags.get("ref");  // e.g., "01MN02" for Muntii Nostri

        if (colour != null) {
            TrailMarking.MarkingColor color =
                TrailMarking.MarkingColor.valueOf(colour.toUpperCase());
            TrailMarking marking = new TrailMarking(
                colour + ":unknown",
                color,
                TrailMarking.MarkingShape.STRIPE  // Default to stripe
            );
            trail.setMarking(marking);
        } else {
            // Last resort: generic marking
            trail.setMarking(new TrailMarking(
                "unknown:unknown",
                TrailMarking.MarkingColor.BLACK,
                TrailMarking.MarkingShape.NONE
            ));
        }
    }
}
```

### 8.2 Handling Broken/Incomplete Relations

```java
@Service
public class OSMRelationValidator {

    /**
     * Check if OSM relation is valid for hiking
     */
    public boolean isValidHikingRelation(OverpassRelation relation) {
        // Check required tags
        if (relation.getTags().get("name") == null) {
            return false;  // Name is mandatory
        }

        // Check if it has members (ways/nodes)
        if (relation.getMembers().isEmpty()) {
            return false;  // Empty route
        }

        // Check if ways are connected (not broken)
        if (!areWaysConnected(relation.getWays())) {
            logger.warn("Trail " + relation.getId() + " has disconnected segments");
            return false;  // Can't use disconnected trails
        }

        // Check if route type is hiking-related
        String routeType = relation.getTags().get("route");
        if (!isHikingRouteType(routeType)) {
            return false;
        }

        return true;
    }

    private boolean areWaysConnected(List<OverpassWay> ways) {
        // Verify ways form a continuous path
        for (int i = 0; i < ways.size() - 1; i++) {
            long lastNode = ways.get(i).getLastNodeId();
            long firstNodeNext = ways.get(i + 1).getFirstNodeId();

            if (lastNode != firstNodeNext) {
                return false;  // Disconnection detected
            }
        }
        return true;
    }

    private boolean isHikingRouteType(String routeType) {
        return routeType != null && (
            routeType.equals("hiking") ||
            routeType.equals("foot") ||
            routeType.equals("alpine_hiking") ||
            routeType.equals("multi")
        );
    }
}
```

### 8.3 Duplicate Detection

```java
@Service
public class DuplicateDetectionService {

    /**
     * Detect duplicate trails from different sources
     */
    public Optional<Trail> findDuplicate(Trail newTrail, TrailRepository repo) {
        // 1. Exact OSM ID match (if OSM source)
        if (newTrail.getOsmId() != null) {
            return repo.findByOsmId(newTrail.getOsmId());
        }

        // 2. Spatial match (same location, similar distance)
        List<Trail> nearby = repo.findTrailsNearLocation(
            newTrail.getWaypoints().get(0).getLatitude(),
            newTrail.getWaypoints().get(0).getLongitude(),
            5.0  // 5km radius
        );

        for (Trail existing : nearby) {
            if (isSameTrail(newTrail, existing)) {
                return Optional.of(existing);
            }
        }

        return Optional.empty();
    }

    /**
     * Compare two trails for similarity
     */
    private boolean isSameTrail(Trail t1, Trail t2) {
        // Name similarity (Levenshtein distance)
        double nameSimilarity = calculateStringSimilarity(t1.getName(), t2.getName());

        // Distance similarity (within 5%)
        double distanceDiff = Math.abs(t1.getDistance() - t2.getDistance()) /
                            Math.max(t1.getDistance(), t2.getDistance());

        // If 90% name match AND distance within 5%, probably same trail
        return nameSimilarity > 0.9 && distanceDiff < 0.05;
    }

    private double calculateStringSimilarity(String s1, String s2) {
        // Levenshtein distance based similarity
        int distance = levenshteinDistance(s1.toLowerCase(), s2.toLowerCase());
        int maxLen = Math.max(s1.length(), s2.length());
        return 1.0 - (double) distance / maxLen;
    }
}
```

---

## 9. IMPLEMENTATION ROADMAP

### Phase 1: Foundation (Weeks 1-2)
- ✓ Set up PostgreSQL + PostGIS
- ✓ Create domain models (Trail, Waypoint, TrailMarking)
- ✓ Implement Overpass API client
- ✓ Build trail normalizer

### Phase 2: Ingestion (Weeks 3-4)
- ✓ Implement OSM ingestion service
- ✓ Add trail validation & quality checks
- ✓ Handle duplicate detection
- ✓ Populate database with Bucegi trails

### Phase 3: API (Weeks 5-6)
- ✓ Implement REST endpoints
- ✓ Add GeoJSON export
- ✓ Add GPX export
- ✓ Create data transfer objects

### Phase 4: Frontend (Weeks 7-8)
- ✓ Render trail markings correctly
- ✓ Implement legend
- ✓ Add filtering by marking/difficulty
- ✓ Create trail detail views

### Phase 5: Polish (Weeks 9-10)
- ✓ Caching layer
- ✓ Performance optimization
- ✓ Edge case handling
- ✓ Documentation & licensing

---

## 10. SUMMARY: Why This Architecture?

**Problem**: Simple mock-data app vs. Production-grade hiking app

**Solution**: OSM-first architecture

**Benefits**:
1. **Authoritative Data** - OSM is maintained by the hiking community
2. **Official Markings** - OSMC standard ensures real-world accuracy
3. **Free & Legal** - ODbL compliant, no licensing issues
4. **Crowdsourced Updates** - Trails improve over time
5. **Extensible** - Can add weather, equipment, difficulty scoring later
6. **Real Hiker Use** - Data used by actual people in the field
7. **Separation of Concerns** - OSM ingestion independent of API/UI

**Trade-offs**:
- Dependency on OSM data quality (mitigated by validation)
- Requires periodic sync (weekly/monthly ingestion runs)
- Need PostGIS for spatial queries
- ODbL compliance (not a limitation, actually a feature)

---

This architecture makes TrailEquip a **professional-grade hiking application** that can grow to support thousands of trails while maintaining data integrity and legal compliance.

