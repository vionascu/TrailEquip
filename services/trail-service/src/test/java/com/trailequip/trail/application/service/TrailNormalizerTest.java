package com.trailequip.trail.application.service;

import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.model.Waypoint;
import com.trailequip.trail.infrastructure.overpass.OverpassRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrailNormalizer service.
 * Tests OSM data normalization to domain objects.
 */
class TrailNormalizerTest {

    private TrailNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new TrailNormalizer();
    }

    @Test
    void shouldNormalizeSimpleTrailFromOSM() {
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(25.540, 45.348, 950));
        coordinates.add(new Coordinate(25.542, 45.350, 1000));
        coordinates.add(new Coordinate(25.544, 45.352, 1050));

        OverpassRelation relation = new OverpassRelation(
            12345L,
            "Test Trail",
            "hiking",
            "01MN",
            "lwn",
            "OpenStreetMap",
            "blue:blue_stripe",
            "moderate",
            "A test trail",
            new ArrayList<>(),
            coordinates
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertNotNull(trail);
        assertEquals("Test Trail", trail.getName());
        assertEquals(12345L, trail.getOsmId());
        assertEquals("01MN", trail.getRef());
        assertEquals("openstreetmap", trail.getSource());
        assertNotNull(trail.getGeometry());
        assertFalse(trail.getGeometry().isEmpty());
    }

    @Test
    void shouldParseOSMCMarkingCorrectly() {
        List<Coordinate> coordinates = createSimpleCoordinates();

        OverpassRelation relation = new OverpassRelation(
            123L, "Trail", "hiking", null, null, null,
            "blue:blue_stripe", null, null, new ArrayList<>(), coordinates
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertNotNull(trail.getMarking());
        assertEquals("blue:blue_stripe", trail.getMarking().getOsmcSymbol());
        assertEquals("#0000FF", trail.getMarking().getHexColor());
    }

    @Test
    void shouldHandleMissingOSMCMarking() {
        List<Coordinate> coordinates = createSimpleCoordinates();

        OverpassRelation relation = new OverpassRelation(
            123L, "Trail", "hiking", null, null, null,
            null, null, null, new ArrayList<>(), coordinates
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertNotNull(trail.getMarking());
        assertEquals("none:none", trail.getMarking().getOsmcSymbol());
    }

    @Test
    void shouldCalculateTrailStatistics() {
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(25.540, 45.348, 950));
        coords.add(new Coordinate(25.542, 45.350, 1100));
        coords.add(new Coordinate(25.544, 45.352, 1050));

        OverpassRelation relation = new OverpassRelation(
            123L, "Trail", "hiking", null, null, null,
            null, null, null, new ArrayList<>(), coords
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertTrue(trail.getDistance() > 0);
        assertTrue(trail.getElevationGain() > 0);
        assertTrue(trail.getDurationMinutes() > 0);
        assertNotNull(trail.getMaxSlope());
    }

    @Test
    void shouldExtractWaypoints() {
        List<Coordinate> coords = new ArrayList<>();
        // Create 20 points to ensure waypoint extraction
        for (int i = 0; i < 20; i++) {
            coords.add(new Coordinate(25.540 + (i * 0.001), 45.348 + (i * 0.001), 950 + (i * 10)));
        }

        OverpassRelation relation = new OverpassRelation(
            123L, "Trail", "hiking", null, null, null,
            null, null, null, new ArrayList<>(), coords
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertNotNull(trail.getWaypoints());
        assertFalse(trail.getWaypoints().isEmpty());
        // Should have start, intermediate points, and end
        assertTrue(trail.getWaypoints().size() >= 3);

        // Check start and end waypoints
        Waypoint start = trail.getWaypoints().get(0);
        assertEquals(Waypoint.WaypointType.START, start.getType());
        assertEquals(0, start.getSequenceOrder());

        Waypoint end = trail.getWaypoints().get(trail.getWaypoints().size() - 1);
        assertEquals(Waypoint.WaypointType.END, end.getType());
    }

    @Test
    void shouldInferDifficultyFromMetrics() {
        List<Coordinate> coords = new ArrayList<>();
        // Create points with high elevation gain
        for (int i = 0; i < 50; i++) {
            coords.add(new Coordinate(25.540, 45.348, 950 + (i * 40))); // 2000m+ gain
        }

        OverpassRelation relation = new OverpassRelation(
            123L, "Hard Trail", "hiking", null, null, null,
            null, null, null, new ArrayList<>(), coords
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertNotNull(trail.getDifficulty());
        // High elevation should infer to at least HARD
        assertTrue(trail.getDifficulty().ordinal() >= 2); // HARD or higher
    }

    @Test
    void shouldMapOSMDifficultyCorrectly() {
        List<Coordinate> coordinates = createSimpleCoordinates();

        // Test "difficult" mapping
        OverpassRelation relation = new OverpassRelation(
            123L, "Trail", "hiking", null, null, null,
            null, "difficult", null, new ArrayList<>(), coordinates
        );

        Trail trail = normalizer.normalizeToDomain(relation);
        assertEquals("HARD", trail.getDifficulty().name());
    }

    @Test
    void shouldClassifyTerrain() {
        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            coords.add(new Coordinate(25.540, 45.348, 2100 + (i * 10)));
        }

        OverpassRelation relation = new OverpassRelation(
            123L, "Alpine Trail", "hiking", null, null, null,
            null, null, null, new ArrayList<>(), coords
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertNotNull(trail.getTerrain());
        assertFalse(trail.getTerrain().isEmpty());
        // High elevation should include alpine_meadow
        assertTrue(trail.getTerrain().contains("alpine_meadow"));
    }

    @Test
    void shouldIdentifyHazards() {
        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            coords.add(new Coordinate(25.540, 45.348, 950 + (i * 35))); // 2000m+ gain
        }

        OverpassRelation relation = new OverpassRelation(
            123L, "Trail in Bucegi", "hiking", null, null, null,
            null, null, null, new ArrayList<>(), coords
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertNotNull(trail.getHazards());
        assertFalse(trail.getHazards().isEmpty());
    }

    @Test
    void shouldEstimateDurationCorrectly() {
        List<Coordinate> coordinates = createSimpleCoordinates();

        OverpassRelation relation = new OverpassRelation(
            123L, "Trail", "hiking", null, null, null,
            null, null, null, new ArrayList<>(), coordinates
        );

        Trail trail = normalizer.normalizeToDomain(relation);

        assertTrue(trail.getDurationMinutes() > 0);
        // Duration should be reasonable: distance/3 km/h = minutes
        assertTrue(trail.getDurationMinutes() <= 2000); // Sanity check
    }

    @Test
    void shouldNormalizeNullFieldsGracefully() {
        List<Coordinate> coordinates = createSimpleCoordinates();

        OverpassRelation relation = new OverpassRelation(
            123L, "Trail", null, null, null, null,
            null, null, null, new ArrayList<>(), coordinates
        );

        assertDoesNotThrow(() -> normalizer.normalizeToDomain(relation));
        Trail trail = normalizer.normalizeToDomain(relation);
        assertNotNull(trail);
    }

    // Helper methods

    private List<Coordinate> createSimpleCoordinates() {
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(25.540, 45.348, 950));
        coords.add(new Coordinate(25.541, 45.349, 1000));
        coords.add(new Coordinate(25.542, 45.350, 1050));
        return coords;
    }
}
