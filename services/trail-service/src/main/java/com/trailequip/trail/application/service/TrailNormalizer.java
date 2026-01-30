package com.trailequip.trail.application.service;

import com.trailequip.trail.domain.model.*;
import com.trailequip.trail.infrastructure.overpass.OverpassRelation;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

/**
 * Service for converting OSM Overpass relations to Trail domain objects.
 * Normalizes OSM data and applies business rules for trail classification.
 */
@Service
public class TrailNormalizer {

    private static final GeometryFactory geometryFactory = new GeometryFactory(
        new PrecisionModel(PrecisionModel.FIXED), 4326
    );

    private static final Pattern OSMC_PATTERN = Pattern.compile(
        "^(\\w+):(\\w+)(?:_(\\w+))?(?:_(\\w+))?$"
    );

    /**
     * Normalize Overpass relation to Trail domain object.
     *
     * @param relation OSM relation from Overpass API
     * @return normalized Trail object
     */
    public Trail normalizeToDomain(OverpassRelation relation) {
        // Create base trail
        Trail trail = new Trail(
            relation.getId(),
            normalizeTrailName(relation.getName()),
            relation.getRef(),
            relation.calculateDistance(),
            inferDifficulty(relation),
            parseTrailMarking(relation.getOsmcSymbol())
        );

        // Set calculated statistics
        trail.setDescription(relation.getDescription());
        trail.setElevationGain(relation.calculateElevationGain());
        trail.setElevationLoss(relation.calculateElevationLoss());
        trail.setMaxElevation(relation.getMaxElevation());

        // Estimate duration (average 3 km/h with 300m elevation = 30 min extra per 300m)
        int durationMinutes = estimateDuration(
            relation.calculateDistance(),
            relation.calculateElevationGain()
        );
        trail.setDurationMinutes(durationMinutes);

        trail.setMaxSlope(relation.calculateMaxSlope());
        trail.setAvgSlope(relation.calculateAvgSlope());
        trail.setSource("openstreetmap");

        // Set terrain classification
        trail.setTerrain(classifyTerrain(relation));

        // Set hazards based on difficulty and characteristics
        trail.setHazards(identifyHazards(relation));

        // Build geometry
        trail.setGeometry(buildLineString(relation.getCoordinates()));

        // Create waypoints from coordinates
        List<Waypoint> waypoints = extractWaypoints(relation, trail);
        trail.setWaypoints(waypoints);

        // Create trail segments from member ways
        List<TrailSegment> segments = createTrailSegments(relation, trail);
        trail.setSegments(segments);

        return trail;
    }

    /**
     * Infer difficulty from OSM relation data.
     * Uses explicit difficulty tag or infers from calculated metrics.
     */
    private Difficulty inferDifficulty(OverpassRelation relation) {
        String osmDifficulty = relation.getDifficulty();

        if (osmDifficulty != null) {
            return mapOsmDifficulty(osmDifficulty);
        }

        // Infer from calculated metrics
        Integer elevationGain = relation.calculateElevationGain();
        Double maxSlope = relation.calculateMaxSlope();

        return Difficulty.inferFromMetrics(elevationGain, maxSlope);
    }

    /**
     * Map OSM hiking:difficulty values to Difficulty enum.
     * OSM uses: easy, moderate, difficult, very_difficult
     */
    private Difficulty mapOsmDifficulty(String osmValue) {
        if (osmValue == null) {
            return Difficulty.MEDIUM;
        }

        return switch (osmValue.toLowerCase()) {
            case "easy", "simple" -> Difficulty.EASY;
            case "moderate", "medium" -> Difficulty.MEDIUM;
            case "difficult", "hard" -> Difficulty.HARD;
            case "very_difficult", "alpine" -> Difficulty.ALPINE;
            case "scrambling", "rock_climbing" -> Difficulty.SCRAMBLING;
            default -> Difficulty.MEDIUM;
        };
    }

    /**
     * Parse OSMC trail marking from OSM tag.
     * Format: "background:foreground_symbol"
     * Example: "blue:blue_stripe", "red:red_triangle:white_rectangle"
     */
    private TrailMarking parseTrailMarking(String osmcSymbol) {
        if (osmcSymbol == null || osmcSymbol.trim().isEmpty()) {
            return new TrailMarking("none:none", TrailMarking.MarkingColor.WHITE, TrailMarking.MarkingShape.NONE);
        }

        try {
            String[] parts = osmcSymbol.split(":");
            if (parts.length < 2) {
                return new TrailMarking(osmcSymbol, TrailMarking.MarkingColor.WHITE, TrailMarking.MarkingShape.NONE);
            }

            String backgroundStr = parts[0].toLowerCase();
            String foregroundStr = parts[1].toLowerCase();

            TrailMarking.MarkingColor color = parseMarkingColor(backgroundStr);
            TrailMarking.MarkingShape shape = parseMarkingShape(foregroundStr);

            return new TrailMarking(osmcSymbol, color, shape);
        } catch (Exception e) {
            // Return default marking if parsing fails
            return new TrailMarking(osmcSymbol, TrailMarking.MarkingColor.WHITE, TrailMarking.MarkingShape.NONE);
        }
    }

    /**
     * Parse color from OSMC symbol component.
     */
    private TrailMarking.MarkingColor parseMarkingColor(String colorStr) {
        return switch (colorStr) {
            case "blue" -> TrailMarking.MarkingColor.BLUE;
            case "red" -> TrailMarking.MarkingColor.RED;
            case "yellow" -> TrailMarking.MarkingColor.YELLOW;
            case "green" -> TrailMarking.MarkingColor.GREEN;
            case "white" -> TrailMarking.MarkingColor.WHITE;
            case "orange" -> TrailMarking.MarkingColor.ORANGE;
            case "black" -> TrailMarking.MarkingColor.BLACK;
            case "purple", "violet" -> TrailMarking.MarkingColor.PURPLE;
            default -> TrailMarking.MarkingColor.WHITE;
        };
    }

    /**
     * Parse shape from OSMC symbol component.
     */
    private TrailMarking.MarkingShape parseMarkingShape(String shapeStr) {
        return switch (shapeStr) {
            case "stripe", "bar" -> TrailMarking.MarkingShape.STRIPE;
            case "triangle", "pyramid" -> TrailMarking.MarkingShape.TRIANGLE;
            case "cross", "plus", "x" -> TrailMarking.MarkingShape.CROSS;
            case "dot", "circle", "point" -> TrailMarking.MarkingShape.DOT;
            case "rectangle", "square", "box" -> TrailMarking.MarkingShape.RECTANGLE;
            case "arch", "arch_down" -> TrailMarking.MarkingShape.ARCH;
            case "none", "blank" -> TrailMarking.MarkingShape.NONE;
            default -> TrailMarking.MarkingShape.NONE;
        };
    }

    /**
     * Classify terrain types from OSM way tags or route characteristics.
     */
    private List<String> classifyTerrain(OverpassRelation relation) {
        Set<String> terrain = new HashSet<>();

        // Default classifications based on elevation and location
        if (relation.getMaxElevation() > 2000) {
            terrain.add("alpine_meadow");
        }

        // Check for scrambling/rock based on slope
        if (relation.calculateMaxSlope() > 30) {
            terrain.add("scramble");
        }

        if (relation.calculateMaxSlope() > 40) {
            terrain.add("rock");
        }

        // Default forest for lower elevation
        if (relation.getMaxElevation() < 1500) {
            terrain.add("forest");
        }

        // High altitude usually has exposed ridges
        if (relation.getMaxElevation() > 2200) {
            terrain.add("exposed_ridge");
        }

        return new ArrayList<>(terrain);
    }

    /**
     * Identify hazards based on trail characteristics and difficulty.
     */
    private List<String> identifyHazards(OverpassRelation relation) {
        Set<String> hazards = new HashSet<>();

        Difficulty difficulty = inferDifficulty(relation);
        double maxSlope = relation.calculateMaxSlope();

        // Add hazards based on difficulty
        if (difficulty == Difficulty.HARD || difficulty == Difficulty.ALPINE || difficulty == Difficulty.SCRAMBLING) {
            hazards.add("exposure");
        }

        if (maxSlope > 25) {
            hazards.add("steep_terrain");
        }

        if (relation.getMaxElevation() > 2300) {
            hazards.add("high_altitude");
        }

        if (difficulty == Difficulty.ALPINE || difficulty == Difficulty.SCRAMBLING) {
            hazards.add("weather_dependent");
        }

        // Bucegi-specific hazards
        if (relation.getName() != null && relation.getName().toLowerCase().contains("bucegi")) {
            hazards.add("bears");
            hazards.add("limited_water_sources");
        }

        return new ArrayList<>(hazards);
    }

    /**
     * Estimate trail duration in minutes.
     * Formula: (distance / 3 km/h) + (elevation_gain / 300m * 30 min)
     */
    private int estimateDuration(Double distance, Integer elevationGain) {
        if (distance == null || distance == 0) {
            return 0;
        }

        double baseDuration = (distance / 3.0) * 60; // minutes at 3 km/h
        double elevationPenalty = 0;

        if (elevationGain != null && elevationGain > 0) {
            elevationPenalty = (elevationGain / 300.0) * 30; // 30 min per 300m elevation
        }

        return (int) (baseDuration + elevationPenalty);
    }

    /**
     * Build LineString geometry from coordinates.
     */
    private LineString buildLineString(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return geometryFactory.createLineString(new Coordinate[0]);
        }

        // Ensure coordinates have elevation data
        Coordinate[] coords = coordinates.stream()
            .map(c -> new Coordinate(c.x, c.y, Double.isNaN(c.z) ? 0 : c.z))
            .toArray(Coordinate[]::new);

        return geometryFactory.createLineString(coords);
    }

    /**
     * Extract key waypoints from trail coordinates.
     * Selects significant elevation changes and endpoints.
     */
    private List<Waypoint> extractWaypoints(OverpassRelation relation, Trail trail) {
        List<Waypoint> waypoints = new ArrayList<>();
        List<Coordinate> coordinates = relation.getCoordinates();

        if (coordinates == null || coordinates.isEmpty()) {
            return waypoints;
        }

        // Add start point
        Coordinate startCoord = coordinates.get(0);
        Waypoint startPoint = new Waypoint(
            startCoord.getY(), startCoord.getX(), (int) startCoord.getZ(),
            "Start: " + relation.getName(), Waypoint.WaypointType.START
        );
        startPoint.setTrail(trail);
        startPoint.setSequenceOrder(0);
        waypoints.add(startPoint);

        // Add significant elevation change points
        int sequenceOrder = 1;
        int step = Math.max(1, coordinates.size() / 10); // Extract ~10 intermediate points

        for (int i = step; i < coordinates.size() - 1; i += step) {
            Coordinate coord = coordinates.get(i);
            Waypoint waypoint = new Waypoint(
                coord.getY(), coord.getX(), (int) coord.getZ(),
                "Waypoint " + sequenceOrder, Waypoint.WaypointType.JUNCTION
            );
            waypoint.setTrail(trail);
            waypoint.setSequenceOrder(sequenceOrder);
            waypoints.add(waypoint);
            sequenceOrder++;
        }

        // Add end point
        Coordinate endCoord = coordinates.get(coordinates.size() - 1);
        Waypoint endPoint = new Waypoint(
            endCoord.getY(), endCoord.getX(), (int) endCoord.getZ(),
            "End: " + relation.getName(), Waypoint.WaypointType.END
        );
        endPoint.setTrail(trail);
        endPoint.setSequenceOrder(sequenceOrder);
        waypoints.add(endPoint);

        return waypoints;
    }

    /**
     * Create trail segments from OSM member ways.
     */
    private List<TrailSegment> createTrailSegments(OverpassRelation relation, Trail trail) {
        List<TrailSegment> segments = new ArrayList<>();
        List<Long> memberWayIds = relation.getMemberWayIds();

        if (memberWayIds == null || memberWayIds.isEmpty()) {
            return segments;
        }

        int sequenceOrder = 0;
        for (Long wayId : memberWayIds) {
            TrailSegment segment = new TrailSegment();
            segment.setTrail(trail);
            segment.setOsmWayId(wayId);
            segment.setSequenceOrder(sequenceOrder++);
            segment.setTerrainType(TrailSegment.TerrainType.FOREST); // Default
            segments.add(segment);
        }

        return segments;
    }

    /**
     * Normalize trail name (handle special characters, formatting).
     */
    private String normalizeTrailName(String name) {
        if (name == null) {
            return "Unnamed Trail";
        }
        return name.trim();
    }
}
