package com.trailequip.trail.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.model.Waypoint;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service for exporting trails in various formats (GeoJSON, GPX, etc.).
 * Used for map visualization and GPS device compatibility.
 */
@Service
@RequiredArgsConstructor
public class TrailExportService {

    private final ObjectMapper objectMapper;

    /**
     * Export trail as GeoJSON Feature.
     * Format suitable for Leaflet, MapBox, and other web mapping libraries.
     */
    public String exportAsGeoJSON(Trail trail) throws Exception {
        ObjectNode feature = objectMapper.createObjectNode();
        feature.put("type", "Feature");

        // Add properties
        ObjectNode properties = objectMapper.createObjectNode();
        properties.put("id", trail.getId().toString());
        properties.put("name", trail.getName());
        properties.put("description", trail.getDescription());
        properties.put("distance", trail.getDistance());
        properties.put("elevationGain", trail.getElevationGain());
        properties.put("elevationLoss", trail.getElevationLoss());
        properties.put("difficulty", trail.getDifficulty().name());
        properties.put("maxSlope", trail.getMaxSlope());
        properties.put("avgSlope", trail.getAvgSlope());
        properties.put("source", trail.getSource());

        if (trail.getRef() != null) {
            properties.put("ref", trail.getRef());
        }

        if (trail.getOsmId() != null) {
            properties.put("osmId", trail.getOsmId());
        }

        // Add terrain as array
        if (trail.getTerrain() != null && !trail.getTerrain().isEmpty()) {
            ArrayNode terrainArray = properties.putArray("terrain");
            trail.getTerrain().forEach(terrainArray::add);
        }

        // Add hazards as array
        if (trail.getHazards() != null && !trail.getHazards().isEmpty()) {
            ArrayNode hazardsArray = properties.putArray("hazards");
            trail.getHazards().forEach(hazardsArray::add);
        }

        feature.set("properties", properties);

        // Add geometry
        feature.set("geometry", buildGeoJSONGeometry(trail.getGeometry()));

        return objectMapper.writeValueAsString(feature);
    }

    /**
     * Export multiple trails as GeoJSON FeatureCollection.
     */
    public String exportAsGeoJSONCollection(List<Trail> trails) throws Exception {
        ObjectNode collection = objectMapper.createObjectNode();
        collection.put("type", "FeatureCollection");

        ArrayNode features = collection.putArray("features");

        for (Trail trail : trails) {
            ObjectNode feature = objectMapper.createObjectNode();
            feature.put("type", "Feature");

            // Properties
            ObjectNode properties = objectMapper.createObjectNode();
            properties.put("id", trail.getId().toString());
            properties.put("name", trail.getName());
            properties.put("distance", trail.getDistance());
            properties.put("difficulty", trail.getDifficulty().name());
            properties.put("source", trail.getSource());

            feature.set("properties", properties);
            feature.set("geometry", buildGeoJSONGeometry(trail.getGeometry()));

            features.add(feature);
        }

        return objectMapper.writeValueAsString(collection);
    }

    /**
     * Build GeoJSON geometry object from LineString.
     */
    private ObjectNode buildGeoJSONGeometry(LineString geometry) {
        ObjectNode geoJson = objectMapper.createObjectNode();
        geoJson.put("type", "LineString");

        ArrayNode coordinates = geoJson.putArray("coordinates");

        if (geometry != null) {
            for (Coordinate coord : geometry.getCoordinates()) {
                ArrayNode coordArray = objectMapper.createArrayNode();
                coordArray.add(coord.getX());  // longitude
                coordArray.add(coord.getY());  // latitude
                if (!Double.isNaN(coord.getZ())) {
                    coordArray.add(coord.getZ());  // elevation
                }
                coordinates.add(coordArray);
            }
        }

        return geoJson;
    }

    /**
     * Export trail as GPX 1.1 format.
     * Format suitable for GPS devices and mapping applications.
     * See: https://www.topografix.com/GPX/1/1/
     */
    public String exportAsGPX(Trail trail) {
        StringBuilder gpx = new StringBuilder();

        // GPX header
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<gpx version=\"1.1\" creator=\"TrailEquip\" ");
        gpx.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ");
        gpx.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        gpx.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");

        // Metadata
        gpx.append("<metadata>\n");
        gpx.append("  <name>").append(escapeXml(trail.getName())).append("</name>\n");

        if (trail.getDescription() != null) {
            gpx.append("  <desc>").append(escapeXml(trail.getDescription())).append("</desc>\n");
        }

        gpx.append("  <author>\n");
        gpx.append("    <name>TrailEquip</name>\n");
        gpx.append("    <link href=\"https://trailequip.com\"/>\n");
        gpx.append("  </author>\n");
        gpx.append("  <time>").append(Instant.now().toString()).append("</time>\n");
        gpx.append("</metadata>\n");

        // Add track
        gpx.append("<trk>\n");
        gpx.append("  <name>").append(escapeXml(trail.getName())).append("</name>\n");

        if (trail.getDescription() != null) {
            gpx.append("  <desc>").append(escapeXml(trail.getDescription())).append("</desc>\n");
        }

        // Extensions with trail metadata
        gpx.append("  <extensions>\n");
        gpx.append("    <difficulty>").append(trail.getDifficulty().name()).append("</difficulty>\n");
        gpx.append("    <distance>").append(trail.getDistance()).append("</distance>\n");

        if (trail.getElevationGain() != null) {
            gpx.append("    <elevationGain>").append(trail.getElevationGain()).append("</elevationGain>\n");
        }

        if (trail.getElevationLoss() != null) {
            gpx.append("    <elevationLoss>").append(trail.getElevationLoss()).append("</elevationLoss>\n");
        }

        if (trail.getDurationMinutes() != null) {
            gpx.append("    <duration>").append(trail.getDurationMinutes()).append("</duration>\n");
        }

        gpx.append("    <source>").append(trail.getSource()).append("</source>\n");
        gpx.append("  </extensions>\n");

        // Track segment with waypoints
        gpx.append("  <trkseg>\n");

        LineString geometry = trail.getGeometry();
        if (geometry != null && !geometry.isEmpty()) {
            for (Coordinate coord : geometry.getCoordinates()) {
                gpx.append("    <trkpt lat=\"").append(coord.getY()).append("\" lon=\"").append(coord.getX()).append("\">\n");

                if (!Double.isNaN(coord.getZ())) {
                    gpx.append("      <ele>").append(coord.getZ()).append("</ele>\n");
                }

                gpx.append("    </trkpt>\n");
            }
        }

        gpx.append("  </trkseg>\n");
        gpx.append("</trk>\n");

        // Add waypoints
        if (trail.getWaypoints() != null && !trail.getWaypoints().isEmpty()) {
            for (Waypoint waypoint : trail.getWaypoints()) {
                gpx.append("<wpt lat=\"").append(waypoint.getLatitude()).append("\" lon=\"").append(waypoint.getLongitude()).append("\">\n");

                if (waypoint.getElevation() != null) {
                    gpx.append("  <ele>").append(waypoint.getElevation()).append("</ele>\n");
                }

                if (waypoint.getName() != null) {
                    gpx.append("  <name>").append(escapeXml(waypoint.getName())).append("</name>\n");
                }

                if (waypoint.getType() != null) {
                    gpx.append("  <type>").append(waypoint.getType().name()).append("</type>\n");
                }

                if (waypoint.getDescription() != null) {
                    gpx.append("  <desc>").append(escapeXml(waypoint.getDescription())).append("</desc>\n");
                }

                gpx.append("</wpt>\n");
            }
        }

        // GPX closing tag
        gpx.append("</gpx>\n");

        return gpx.toString();
    }

    /**
     * Export multiple trails as GPX.
     * Each trail becomes a separate track in the GPX file.
     */
    public String exportAsGPXCollection(List<Trail> trails) {
        StringBuilder gpx = new StringBuilder();

        // GPX header
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<gpx version=\"1.1\" creator=\"TrailEquip\" ");
        gpx.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ");
        gpx.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        gpx.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");

        // Metadata
        gpx.append("<metadata>\n");
        gpx.append("  <name>TrailEquip Collection</name>\n");
        gpx.append("  <desc>Multiple hiking trails</desc>\n");
        gpx.append("  <author>\n");
        gpx.append("    <name>TrailEquip</name>\n");
        gpx.append("    <link href=\"https://trailequip.com\"/>\n");
        gpx.append("  </author>\n");
        gpx.append("  <time>").append(Instant.now().toString()).append("</time>\n");
        gpx.append("</metadata>\n");

        // Add all trails as tracks
        for (Trail trail : trails) {
            gpx.append("<trk>\n");
            gpx.append("  <name>").append(escapeXml(trail.getName())).append("</name>\n");

            gpx.append("  <trkseg>\n");

            LineString geometry = trail.getGeometry();
            if (geometry != null && !geometry.isEmpty()) {
                for (Coordinate coord : geometry.getCoordinates()) {
                    gpx.append("    <trkpt lat=\"").append(coord.getY()).append("\" lon=\"").append(coord.getX()).append("\">\n");

                    if (!Double.isNaN(coord.getZ())) {
                        gpx.append("      <ele>").append(coord.getZ()).append("</ele>\n");
                    }

                    gpx.append("    </trkpt>\n");
                }
            }

            gpx.append("  </trkseg>\n");
            gpx.append("</trk>\n");
        }

        // GPX closing tag
        gpx.append("</gpx>\n");

        return gpx.toString();
    }

    /**
     * Escape XML special characters.
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
