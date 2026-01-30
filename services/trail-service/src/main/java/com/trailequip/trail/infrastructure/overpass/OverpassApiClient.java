package com.trailequip.trail.infrastructure.overpass;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Client for querying OpenStreetMap hiking routes via Overpass API.
 * Fetches trail relations and their geometry from OSM.
 */
@Component
public class OverpassApiClient {

    private static final String OVERPASS_API_URL = "https://overpass-api.de/api/interpreter";
    private static final long REQUEST_DELAY_MS = 3000;  // Rate limiting for Overpass API
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 60000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private long lastRequestTime = 0;

    @Value("${overpass.timeout:60000}")
    private int timeoutMs;

    public OverpassApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Query hiking routes in Bucegi Mountains region.
     * Bucegi bounding box: North 45.50, South 45.20, East 25.70, West 25.40
     *
     * @return List of OverpassRelation objects representing hiking trails
     */
    public List<OverpassRelation> queryBucegiHikingRoutes() {
        return queryHikingRoutesByBbox(45.20, 25.40, 45.50, 25.70);
    }

    /**
     * Query hiking routes by geographic bounding box.
     *
     * @param south minimum latitude
     * @param west minimum longitude
     * @param north maximum latitude
     * @param east maximum longitude
     * @return List of OverpassRelation objects
     */
    public List<OverpassRelation> queryHikingRoutesByBbox(double south, double west, double north, double east) {
        String query = buildHikingRoutesQuery(south, west, north, east);
        return executeQuery(query);
    }

    /**
     * Query a specific trail by OSM relation ID.
     *
     * @param relationId OpenStreetMap relation ID
     * @return OverpassRelation object or null if not found
     */
    public OverpassRelation queryTrailById(Long relationId) {
        String query = String.format(
            "[out:json];relation(%d);out geom;",
            relationId
        );
        List<OverpassRelation> results = executeQuery(query);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Query nearby trails for a specific coordinate.
     *
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusKm search radius in kilometers
     * @return List of OverpassRelation objects
     */
    public List<OverpassRelation> queryTrailsNearby(double latitude, double longitude, double radiusKm) {
        // Convert radius from km to degrees (approximate)
        double radiusDegrees = radiusKm / 111.0;
        double south = latitude - radiusDegrees;
        double north = latitude + radiusDegrees;
        double west = longitude - radiusDegrees;
        double east = longitude + radiusDegrees;

        return queryHikingRoutesByBbox(south, west, north, east);
    }

    /**
     * Build Overpass QL query for hiking routes.
     * Includes relations tagged as hiking, foot, or alpine_hiking routes.
     */
    private String buildHikingRoutesQuery(double south, double west, double north, double east) {
        return String.format(
            "[out:json];" +
            "[bbox:%f,%f,%f,%f];" +
            "(" +
            "  relation[type=route][route=hiking];" +
            "  relation[type=route][route=foot];" +
            "  relation[type=route][route=alpine_hiking];" +
            ");" +
            "out geom;",
            south, west, north, east
        );
    }

    /**
     * Execute an Overpass query with rate limiting and retry logic.
     */
    private List<OverpassRelation> executeQuery(String query) {
        enforceRateLimit();

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String response = sendRequest(query);
                return parseResponse(response);
            } catch (Exception e) {
                if (attempt == MAX_RETRIES - 1) {
                    throw new OverpassApiException("Failed to query Overpass API after " + MAX_RETRIES + " attempts", e);
                }
                // Exponential backoff before retry
                try {
                    Thread.sleep((long) (REQUEST_DELAY_MS * Math.pow(2, attempt)));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new OverpassApiException("Interrupted while retrying Overpass query", ie);
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * Send POST request to Overpass API.
     */
    private String sendRequest(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "TrailEquip/1.0 (https://github.com/trailequip/trailequip)");

        HttpEntity<String> request = new HttpEntity<>("data=" + query, headers);

        return restTemplate.postForObject(OVERPASS_API_URL, request, String.class);
    }

    /**
     * Parse Overpass JSON response into OverpassRelation objects.
     */
    private List<OverpassRelation> parseResponse(String jsonResponse) throws Exception {
        List<OverpassRelation> relations = new ArrayList<>();
        JsonNode root = objectMapper.readTree(jsonResponse);

        // Check for errors in response
        if (root.has("remark")) {
            throw new OverpassApiException("Overpass API error: " + root.get("remark").asText());
        }

        JsonNode elements = root.get("elements");
        if (elements == null || !elements.isArray()) {
            return relations;
        }

        Map<Long, OverpassWay> ways = new HashMap<>();
        List<Long> relationIds = new ArrayList<>();

        // First pass: collect ways and find relations
        for (JsonNode element : elements) {
            String type = element.get("type").asText();

            if ("way".equals(type)) {
                OverpassWay way = parseWay(element);
                ways.put(way.getId(), way);
            } else if ("relation".equals(type)) {
                relationIds.add(element.get("id").asLong());
            }
        }

        // Second pass: build relations with their member geometries
        for (JsonNode element : elements) {
            String type = element.get("type").asText();

            if ("relation".equals(type)) {
                OverpassRelation relation = parseRelation(element, ways);
                if (relation != null) {
                    relations.add(relation);
                }
            }
        }

        return relations;
    }

    /**
     * Parse OSM relation element.
     */
    private OverpassRelation parseRelation(JsonNode element, Map<Long, OverpassWay> ways) {
        long id = element.get("id").asLong();
        JsonNode tags = element.get("tags");

        if (tags == null) {
            return null;
        }

        String name = getTagValue(tags, "name");
        String route = getTagValue(tags, "route");
        String ref = getTagValue(tags, "ref");
        String network = getTagValue(tags, "network");
        String operator = getTagValue(tags, "operator");
        String osmcSymbol = getTagValue(tags, "osmc:symbol");
        String difficulty = getTagValue(tags, "hiking:difficulty");
        String description = getTagValue(tags, "description");

        // Parse members to build geometry
        List<Long> memberIds = new ArrayList<>();
        JsonNode members = element.get("members");
        if (members != null && members.isArray()) {
            for (JsonNode member : members) {
                String memberType = member.get("type").asText();
                if ("way".equals(memberType)) {
                    long wayId = member.get("ref").asLong();
                    memberIds.add(wayId);
                }
            }
        }

        // Build combined geometry from member ways
        List<Coordinate> coordinates = buildGeometryFromWays(memberIds, ways);

        if (coordinates.isEmpty()) {
            return null;
        }

        return new OverpassRelation(
            id, name, route, ref, network, operator, osmcSymbol,
            difficulty, description, memberIds, coordinates
        );
    }

    /**
     * Parse OSM way element.
     */
    private OverpassWay parseWay(JsonNode element) {
        long id = element.get("id").asLong();
        List<Coordinate> coordinates = new ArrayList<>();

        JsonNode geometry = element.get("geometry");
        if (geometry != null && geometry.isArray()) {
            for (JsonNode node : geometry) {
                double lat = node.get("lat").asDouble();
                double lon = node.get("lon").asDouble();
                double elevation = node.has("elevation") ? node.get("elevation").asDouble() : 0;
                coordinates.add(new Coordinate(lon, lat, elevation));
            }
        }

        return new OverpassWay(id, coordinates);
    }

    /**
     * Build combined LineString geometry from multiple ways.
     * Ensures proper ordering of ways to form continuous path.
     */
    private List<Coordinate> buildGeometryFromWays(List<Long> memberIds, Map<Long, OverpassWay> ways) {
        if (memberIds.isEmpty() || ways.isEmpty()) {
            return Collections.emptyList();
        }

        List<Coordinate> combined = new ArrayList<>();
        Set<Long> used = new HashSet<>();

        // Start with first way
        long currentWayId = memberIds.stream()
            .filter(ways::containsKey)
            .findFirst()
            .orElse(memberIds.get(0));

        OverpassWay currentWay = ways.get(currentWayId);
        if (currentWay != null) {
            combined.addAll(currentWay.getCoordinates());
            used.add(currentWayId);
        }

        // Connect remaining ways
        while (used.size() < memberIds.size()) {
            Coordinate lastCoord = combined.get(combined.size() - 1);
            boolean found = false;

            for (Long wayId : memberIds) {
                if (used.contains(wayId)) {
                    continue;
                }

                OverpassWay way = ways.get(wayId);
                if (way == null || way.getCoordinates().isEmpty()) {
                    continue;
                }

                // Check if way connects to current endpoint
                Coordinate wayStart = way.getCoordinates().get(0);
                Coordinate wayEnd = way.getCoordinates().get(way.getCoordinates().size() - 1);

                if (coordinatesMatch(lastCoord, wayStart)) {
                    combined.addAll(way.getCoordinates());
                    used.add(wayId);
                    found = true;
                    break;
                } else if (coordinatesMatch(lastCoord, wayEnd)) {
                    // Add way in reverse order
                    List<Coordinate> reversed = new ArrayList<>(way.getCoordinates());
                    Collections.reverse(reversed);
                    combined.addAll(reversed);
                    used.add(wayId);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // If no connecting way found, add remaining ways
                for (Long wayId : memberIds) {
                    if (!used.contains(wayId) && ways.containsKey(wayId)) {
                        combined.addAll(ways.get(wayId).getCoordinates());
                        used.add(wayId);
                    }
                }
                break;
            }
        }

        return combined;
    }

    /**
     * Check if two coordinates are approximately equal (within 0.0001 degrees).
     */
    private boolean coordinatesMatch(Coordinate c1, Coordinate c2) {
        double tolerance = 0.0001;  // ~11 meters
        return Math.abs(c1.getX() - c2.getX()) < tolerance &&
               Math.abs(c1.getY() - c2.getY()) < tolerance;
    }

    /**
     * Extract string value from tags JSON node.
     */
    private String getTagValue(JsonNode tags, String key) {
        if (tags.has(key)) {
            return tags.get(key).asText();
        }
        return null;
    }

    /**
     * Enforce rate limiting - wait at least REQUEST_DELAY_MS between requests.
     */
    private void enforceRateLimit() {
        long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
        if (timeSinceLastRequest < REQUEST_DELAY_MS) {
            try {
                Thread.sleep(REQUEST_DELAY_MS - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * Data class representing an OSM Way with its coordinates.
     */
    public static class OverpassWay {
        private final Long id;
        private final List<Coordinate> coordinates;

        public OverpassWay(Long id, List<Coordinate> coordinates) {
            this.id = id;
            this.coordinates = coordinates;
        }

        public Long getId() {
            return id;
        }

        public List<Coordinate> getCoordinates() {
            return coordinates;
        }
    }

    /**
     * Custom exception for Overpass API errors.
     */
    public static class OverpassApiException extends RuntimeException {
        public OverpassApiException(String message) {
            super(message);
        }

        public OverpassApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
