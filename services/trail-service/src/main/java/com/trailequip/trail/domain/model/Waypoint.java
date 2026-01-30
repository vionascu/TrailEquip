package com.trailequip.trail.domain.model;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Waypoint represents a route stop or point of interest along a trail.
 * Can be a shelter, peak, water source, junction, or scenic viewpoint.
 */
@Entity
@Table(name = "trail_waypoints", indexes = {
    @Index(name = "idx_trail_id", columnList = "trail_id"),
    @Index(name = "idx_osm_node_id", columnList = "osm_node_id"),
    @Index(name = "idx_waypoint_type", columnList = "type")
})
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trail_id")
    private Trail trail;

    @Column(name = "osm_node_id")
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
        SHELTER("Mountain refuge/cabin", "🏠"),
        PEAK("Mountain summit", "⛰️"),
        WATER("Water source", "💧"),
        JUNCTION("Trail junction", "⛳"),
        START("Trail start", "🟢"),
        END("Trail end", "🔴"),
        CAMPING("Camping area", "⛺"),
        VIEWPOINT("Scenic viewpoint", "🔭"),
        OTHER("Other point of interest", "📍");

        private final String description;
        private final String emoji;

        WaypointType(String description, String emoji) {
            this.description = description;
            this.emoji = emoji;
        }

        public String getDescription() {
            return description;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    // ===== CONSTRUCTORS =====

    public Waypoint() {}

    public Waypoint(Double latitude, Double longitude, Integer elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public Waypoint(Double latitude, Double longitude, Integer elevation,
                   String name, WaypointType type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.name = name;
        this.type = type;
    }

    // ===== GETTERS =====

    public UUID getId() {
        return id;
    }

    public Trail getTrail() {
        return trail;
    }

    public Long getOsmNodeId() {
        return osmNodeId;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getElevation() {
        return elevation;
    }

    public String getName() {
        return name;
    }

    public WaypointType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    // ===== SETTERS =====

    public void setTrail(Trail trail) {
        this.trail = trail;
    }

    public void setOsmNodeId(Long osmNodeId) {
        this.osmNodeId = osmNodeId;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setElevation(Integer elevation) {
        this.elevation = elevation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(WaypointType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
