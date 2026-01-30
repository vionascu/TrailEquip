package com.trailequip.trail.domain.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.LineString;
import java.util.UUID;

/**
 * TrailSegment represents a portion of a trail (OSM way).
 * Multiple segments make up the complete trail path.
 */
@Entity
@Table(name = "trail_segments", indexes = {
    @Index(name = "idx_trail_id", columnList = "trail_id"),
    @Index(name = "idx_osm_way_id", columnList = "osm_way_id"),
    @Index(name = "idx_terrain_type", columnList = "terrain_type")
})
public class TrailSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trail_id", nullable = false)
    private Trail trail;

    @Column(nullable = false, name = "osm_way_id")
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

        TerrainType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ===== CONSTRUCTORS =====

    public TrailSegment() {}

    public TrailSegment(Trail trail, Long osmWayId, Integer sequenceOrder,
                       LineString geometry, Double length, TerrainType terrainType) {
        this.trail = trail;
        this.osmWayId = osmWayId;
        this.sequenceOrder = sequenceOrder;
        this.geometry = geometry;
        this.length = length;
        this.terrainType = terrainType;
        this.accessible = true;
    }

    // ===== GETTERS =====

    public UUID getId() {
        return id;
    }

    public Trail getTrail() {
        return trail;
    }

    public Long getOsmWayId() {
        return osmWayId;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public Double getLength() {
        return length;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public Boolean isAccessible() {
        return accessible;
    }

    public String getNotes() {
        return notes;
    }

    // ===== SETTERS =====

    public void setTrail(Trail trail) {
        this.trail = trail;
    }

    public void setOsmWayId(Long osmWayId) {
        this.osmWayId = osmWayId;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public void setAccessible(Boolean accessible) {
        this.accessible = accessible;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
