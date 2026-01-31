package com.trailequip.trail.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.locationtech.jts.geom.LineString;

@Entity
@Table(
        name = "trails",
        indexes = {
            @Index(name = "idx_osm_id", columnList = "osm_id", unique = true),
            @Index(name = "idx_difficulty", columnList = "difficulty"),
            @Index(name = "idx_source", columnList = "source")
        })
public class Trail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // === OSM METADATA ===
    @Column(name = "osm_id", unique = true)
    private Long osmId; // OpenStreetMap relation ID

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String ref; // Reference: "01MN02", "02MN06"

    // === TRAIL GEOMETRY ===
    // NOTE: Disabled for local development (requires PostGIS)
    // Enable when using PostgreSQL with PostGIS extension
    // @Column(columnDefinition = "Geometry(LineString,4326)")
    @Transient
    private LineString geometry; // PostgreSQL PostGIS LineString (local dev: transient)

    // === TRAIL STATISTICS ===
    @Column(nullable = false)
    private Double distance;

    private Integer elevationGain;
    private Integer elevationLoss;
    private Integer durationMinutes;
    private Double maxSlope;
    private Double avgSlope;
    private Integer maxElevation;

    // === TRAIL CLASSIFICATION ===
    @ElementCollection
    @CollectionTable(name = "trail_terrain")
    private List<String> terrain;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @ElementCollection
    @CollectionTable(name = "trail_hazards")
    private List<String> hazards;

    // === TRAIL MARKING (OSMC STANDARD) ===
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
    private TrailMarking marking;

    // === TRAIL WAYPOINTS & SEGMENTS ===
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "trail_id")
    private List<Waypoint> waypoints;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "trail_id")
    private List<TrailSegment> segments;

    // === METADATA ===
    private String source; // "openstreetmap", "muntii-nostri", "wikiloc"

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // ===== CONSTRUCTORS =====

    public Trail() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Trail(
            String name,
            String description,
            Double distance,
            Integer elevationGain,
            Integer elevationLoss,
            Integer durationMinutes,
            Double maxSlope,
            Double avgSlope,
            List<String> terrain,
            Difficulty difficulty,
            List<String> hazards,
            String source) {
        this.name = name;
        this.description = description;
        this.distance = distance;
        this.elevationGain = elevationGain;
        this.elevationLoss = elevationLoss;
        this.durationMinutes = durationMinutes;
        this.maxSlope = maxSlope;
        this.avgSlope = avgSlope;
        this.terrain = terrain;
        this.difficulty = difficulty;
        this.hazards = hazards;
        this.source = source;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Constructor for OSM data
    public Trail(Long osmId, String name, String ref, Double distance, Difficulty difficulty, TrailMarking marking) {
        this.osmId = osmId;
        this.name = name;
        this.ref = ref;
        this.distance = distance;
        this.difficulty = difficulty;
        this.marking = marking;
        this.source = "openstreetmap";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // ===== GETTERS =====

    public UUID getId() {
        return id;
    }

    public Long getOsmId() {
        return osmId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRef() {
        return ref;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getElevationGain() {
        return elevationGain;
    }

    public Integer getElevationLoss() {
        return elevationLoss;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Double getMaxSlope() {
        return maxSlope;
    }

    public Double getAvgSlope() {
        return avgSlope;
    }

    public Integer getMaxElevation() {
        return maxElevation;
    }

    public List<String> getTerrain() {
        return terrain;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public List<String> getHazards() {
        return hazards;
    }

    public TrailMarking getMarking() {
        return marking;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public List<TrailSegment> getSegments() {
        return segments;
    }

    public String getSource() {
        return source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ===== SETTERS =====

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setMarking(TrailMarking marking) {
        this.marking = marking;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public void setSegments(List<TrailSegment> segments) {
        this.segments = segments;
    }

    public void setElevationGain(Integer elevationGain) {
        this.elevationGain = elevationGain;
    }

    public void setElevationLoss(Integer elevationLoss) {
        this.elevationLoss = elevationLoss;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTerrain(List<String> terrain) {
        this.terrain = terrain;
    }

    public void setHazards(List<String> hazards) {
        this.hazards = hazards;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMaxElevation(Integer maxElevation) {
        this.maxElevation = maxElevation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setMaxSlope(Double maxSlope) {
        this.maxSlope = maxSlope;
    }

    public void setAvgSlope(Double avgSlope) {
        this.avgSlope = avgSlope;
    }
}
