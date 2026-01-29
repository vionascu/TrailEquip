package com.trailequip.trail.domain.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Waypoint {
    private Double latitude;
    private Double longitude;
    private Integer elevation;
    private String name;
    private String description;

    public Waypoint() {}

    public Waypoint(Double latitude, Double longitude, Integer elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
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

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
