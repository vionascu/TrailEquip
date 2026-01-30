package com.trailequip.trail.infrastructure.overpass;

import org.locationtech.jts.geom.Coordinate;
import java.util.List;

/**
 * Represents an OSM hiking route relation fetched from Overpass API.
 * Contains all metadata and geometry needed to construct a Trail domain object.
 */
public class OverpassRelation {

    private final Long id;
    private final String name;
    private final String route;  // "hiking", "foot", "alpine_hiking"
    private final String ref;    // Route reference like "01MN02"
    private final String network; // "lwn", "iwn", etc.
    private final String operator;
    private final String osmcSymbol; // OSMC trail marking
    private final String difficulty; // OSM hiking difficulty
    private final String description;
    private final List<Long> memberWayIds; // OSM way IDs that compose this route
    private final List<Coordinate> coordinates; // Combined geometry

    public OverpassRelation(
        Long id,
        String name,
        String route,
        String ref,
        String network,
        String operator,
        String osmcSymbol,
        String difficulty,
        String description,
        List<Long> memberWayIds,
        List<Coordinate> coordinates
    ) {
        this.id = id;
        this.name = name;
        this.route = route;
        this.ref = ref;
        this.network = network;
        this.operator = operator;
        this.osmcSymbol = osmcSymbol;
        this.difficulty = difficulty;
        this.description = description;
        this.memberWayIds = memberWayIds;
        this.coordinates = coordinates;
    }

    // ===== GETTERS =====

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoute() {
        return route;
    }

    public String getRef() {
        return ref;
    }

    public String getNetwork() {
        return network;
    }

    public String getOperator() {
        return operator;
    }

    public String getOsmcSymbol() {
        return osmcSymbol;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description;
    }

    public List<Long> getMemberWayIds() {
        return memberWayIds;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    /**
     * Calculate total distance of route based on coordinates.
     * Uses Haversine formula for great circle distance.
     *
     * @return distance in kilometers
     */
    public Double calculateDistance() {
        if (coordinates == null || coordinates.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            totalDistance += haversineDistance(
                coordinates.get(i),
                coordinates.get(i + 1)
            );
        }
        return totalDistance;
    }

    /**
     * Calculate elevation gain from coordinates.
     *
     * @return elevation gain in meters
     */
    public Integer calculateElevationGain() {
        if (coordinates == null || coordinates.size() < 2) {
            return 0;
        }

        int totalGain = 0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate current = coordinates.get(i);
            Coordinate next = coordinates.get(i + 1);

            double elevationDiff = next.getZ() - current.getZ();
            if (elevationDiff > 0) {
                totalGain += elevationDiff;
            }
        }
        return totalGain;
    }

    /**
     * Calculate elevation loss from coordinates.
     *
     * @return elevation loss in meters
     */
    public Integer calculateElevationLoss() {
        if (coordinates == null || coordinates.size() < 2) {
            return 0;
        }

        int totalLoss = 0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate current = coordinates.get(i);
            Coordinate next = coordinates.get(i + 1);

            double elevationDiff = current.getZ() - next.getZ();
            if (elevationDiff > 0) {
                totalLoss += elevationDiff;
            }
        }
        return totalLoss;
    }

    /**
     * Calculate maximum elevation along the route.
     *
     * @return maximum elevation in meters
     */
    public Integer getMaxElevation() {
        if (coordinates == null || coordinates.isEmpty()) {
            return 0;
        }

        return (int) coordinates.stream()
            .mapToDouble(Coordinate::getZ)
            .max()
            .orElse(0);
    }

    /**
     * Calculate maximum slope of the route.
     * Slope = (elevation change / horizontal distance) * 100
     *
     * @return maximum slope in percentage
     */
    public Double calculateMaxSlope() {
        if (coordinates == null || coordinates.size() < 2) {
            return 0.0;
        }

        double maxSlope = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate current = coordinates.get(i);
            Coordinate next = coordinates.get(i + 1);

            double horizontalDistance = haversineDistance(current, next) * 1000; // to meters
            double elevationDifference = Math.abs(next.getZ() - current.getZ());

            if (horizontalDistance > 0) {
                double slope = (elevationDifference / horizontalDistance) * 100;
                maxSlope = Math.max(maxSlope, slope);
            }
        }

        return maxSlope;
    }

    /**
     * Calculate average slope of the route.
     *
     * @return average slope in percentage
     */
    public Double calculateAvgSlope() {
        if (coordinates == null || coordinates.size() < 2) {
            return 0.0;
        }

        double totalDistance = calculateDistance() * 1000; // to meters
        double totalElevationGain = calculateElevationGain();

        if (totalDistance == 0) {
            return 0.0;
        }

        return (totalElevationGain / totalDistance) * 100;
    }

    /**
     * Haversine formula for calculating great circle distance between two points.
     *
     * @param c1 first coordinate (lon, lat)
     * @param c2 second coordinate (lon, lat)
     * @return distance in kilometers
     */
    private Double haversineDistance(Coordinate c1, Coordinate c2) {
        final int EARTH_RADIUS_KM = 6371;

        double lat1 = Math.toRadians(c1.getY());
        double lat2 = Math.toRadians(c2.getY());
        double deltaLat = Math.toRadians(c2.getY() - c1.getY());
        double deltaLon = Math.toRadians(c2.getX() - c1.getX());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                  Math.cos(lat1) * Math.cos(lat2) *
                  Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String toString() {
        return "OverpassRelation{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", route='" + route + '\'' +
            ", ref='" + ref + '\'' +
            ", coordinates=" + (coordinates != null ? coordinates.size() : 0) + " points" +
            '}';
    }
}
