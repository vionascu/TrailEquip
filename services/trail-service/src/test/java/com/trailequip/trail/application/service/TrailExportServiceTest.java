package com.trailequip.trail.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.model.Waypoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrailExportService.
 * Tests GeoJSON and GPX export functionality.
 */
class TrailExportServiceTest {

    private TrailExportService exportService;
    private ObjectMapper objectMapper;
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        exportService = new TrailExportService(objectMapper);
        geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED), 4326);
    }

    @Test
    void shouldExportTrailAsGeoJSON() throws Exception {
        Trail trail = createSimpleTrail();

        String geoJson = exportService.exportAsGeoJSON(trail);

        assertNotNull(geoJson);
        assertFalse(geoJson.isEmpty());

        JsonNode root = objectMapper.readTree(geoJson);
        assertEquals("Feature", root.get("type").asText());
        assertTrue(root.has("properties"));
        assertTrue(root.has("geometry"));
    }

    @Test
    void shouldIncludeTrailMetadataInGeoJSON() throws Exception {
        Trail trail = createSimpleTrail();
        trail.setName("Test Trail");
        trail.setDistance(15.5);
        trail.setDifficulty(Difficulty.HARD);

        String geoJson = exportService.exportAsGeoJSON(trail);
        JsonNode root = objectMapper.readTree(geoJson);
        JsonNode props = root.get("properties");

        assertEquals("Test Trail", props.get("name").asText());
        assertEquals(15.5, props.get("distance").asDouble());
        assertEquals("HARD", props.get("difficulty").asText());
    }

    @Test
    void shouldExportAsGeoJSONCollection() throws Exception {
        List<Trail> trails = new ArrayList<>();
        trails.add(createSimpleTrail());
        trails.add(createSimpleTrail());

        String geoJson = exportService.exportAsGeoJSONCollection(trails);

        assertNotNull(geoJson);
        JsonNode root = objectMapper.readTree(geoJson);
        assertEquals("FeatureCollection", root.get("type").asText());
        assertEquals(2, root.get("features").size());
    }

    @Test
    void shouldExportTrailAsGPX() throws Exception {
        Trail trail = createSimpleTrail();
        trail.setName("Test Trail");

        String gpx = exportService.exportAsGPX(trail);

        assertNotNull(gpx);
        assertTrue(gpx.contains("<?xml version"));
        assertTrue(gpx.contains("<gpx"));
        assertTrue(gpx.contains("<trk>"));
        assertTrue(gpx.contains("<name>Test Trail</name>"));
        assertTrue(gpx.contains("</gpx>"));
    }

    @Test
    void shouldIncludeTrackpointsInGPX() throws Exception {
        Trail trail = createSimpleTrail();

        String gpx = exportService.exportAsGPX(trail);

        assertTrue(gpx.contains("<trkpt"));
        assertTrue(gpx.contains("lat="));
        assertTrue(gpx.contains("lon="));
        assertTrue(gpx.contains("<ele>"));
    }

    @Test
    void shouldIncludeWaypointsInGPX() throws Exception {
        Trail trail = createSimpleTrail();
        Waypoint waypoint = new Waypoint(45.35, 25.54, 1000, "Test Waypoint", Waypoint.WaypointType.PEAK);
        trail.setWaypoints(List.of(waypoint));

        String gpx = exportService.exportAsGPX(trail);

        assertTrue(gpx.contains("<wpt"));
        assertTrue(gpx.contains("Test Waypoint"));
        assertTrue(gpx.contains("<type>PEAK</type>"));
    }

    @Test
    void shouldIncludeMetadataInGPXExtensions() throws Exception {
        Trail trail = createSimpleTrail();
        trail.setDifficulty(Difficulty.HARD);
        trail.setDistance(20.0);
        trail.setElevationGain(1500);

        String gpx = exportService.exportAsGPX(trail);

        assertTrue(gpx.contains("<difficulty>HARD</difficulty>"));
        assertTrue(gpx.contains("<distance>20"));
        assertTrue(gpx.contains("<elevationGain>1500</elevationGain>"));
    }

    @Test
    void shouldEscapeXMLCharactersInGPX() throws Exception {
        Trail trail = createSimpleTrail();
        trail.setName("Trail & <Special> \"Chars\"");
        trail.setDescription("Description with <tags> & ampersands");

        String gpx = exportService.exportAsGPX(trail);

        assertTrue(gpx.contains("&amp;"));
        assertTrue(gpx.contains("&lt;"));
        assertTrue(gpx.contains("&gt;"));
        assertTrue(gpx.contains("&quot;"));
    }

    @Test
    void shouldHandleNullValuesInGeoJSON() throws Exception {
        Trail trail = new Trail();
        trail.setId(UUID.randomUUID());
        trail.setName("Trail");
        trail.setDistance(10.0);
        trail.setDifficulty(Difficulty.EASY);
        trail.setGeometry(geometryFactory.createLineString(new Coordinate[] {
            new Coordinate(25.54, 45.35, 1000)
        }));

        String geoJson = exportService.exportAsGeoJSON(trail);

        assertNotNull(geoJson);
        assertFalse(geoJson.isEmpty());
    }

    @Test
    void shouldHandleNullValuesInGPX() throws Exception {
        Trail trail = new Trail();
        trail.setId(UUID.randomUUID());
        trail.setName("Trail");
        trail.setGeometry(geometryFactory.createLineString(new Coordinate[] {
            new Coordinate(25.54, 45.35, 1000)
        }));

        String gpx = exportService.exportAsGPX(trail);

        assertNotNull(gpx);
        assertTrue(gpx.contains("<gpx"));
    }

    @Test
    void shouldExportMultipleTrailsAsGPX() throws Exception {
        List<Trail> trails = new ArrayList<>();
        trails.add(createSimpleTrail());
        trails.add(createSimpleTrail());

        String gpx = exportService.exportAsGPXCollection(trails);

        assertNotNull(gpx);
        assertTrue(gpx.contains("<trk>"));
        // Should have 2 tracks
        long trackCount = gpx.split("<trk>").length - 1;
        assertEquals(2, trackCount);
    }

    @Test
    void shouldProduceValidGeoJSONStructure() throws Exception {
        Trail trail = createSimpleTrail();

        String geoJson = exportService.exportAsGeoJSON(trail);
        JsonNode root = objectMapper.readTree(geoJson);

        // Validate GeoJSON structure
        assertTrue(root.has("type"));
        assertTrue(root.has("properties"));
        assertTrue(root.has("geometry"));

        JsonNode geometry = root.get("geometry");
        assertEquals("LineString", geometry.get("type").asText());
        assertTrue(geometry.get("coordinates").isArray());
    }

    @Test
    void shouldProduceValidGPXStructure() throws Exception {
        Trail trail = createSimpleTrail();

        String gpx = exportService.exportAsGPX(trail);

        assertTrue(gpx.startsWith("<?xml"));
        assertTrue(gpx.contains("version=\"1.0\""));
        assertTrue(gpx.contains("xmlns=\"http://www.topografix.com/GPX/1/1\""));
        assertTrue(gpx.contains("<metadata>"));
        assertTrue(gpx.contains("<trk>"));
        assertTrue(gpx.contains("</gpx>"));
    }

    // Helper methods

    private Trail createSimpleTrail() {
        Trail trail = new Trail();
        trail.setId(UUID.randomUUID());
        trail.setName("Test Trail");
        trail.setDistance(10.0);
        trail.setElevationGain(500);
        trail.setElevationLoss(500);
        trail.setDurationMinutes(120);
        trail.setDifficulty(Difficulty.EASY);
        trail.setSource("test");

        Coordinate[] coords = {
            new Coordinate(25.54, 45.35, 1000),
            new Coordinate(25.541, 45.351, 1050),
            new Coordinate(25.542, 45.352, 1100)
        };
        trail.setGeometry(geometryFactory.createLineString(coords));

        return trail;
    }
}
