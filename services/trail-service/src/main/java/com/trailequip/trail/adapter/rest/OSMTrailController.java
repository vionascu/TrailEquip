package com.trailequip.trail.adapter.rest;

import com.trailequip.trail.application.service.OSMIngestionService;
import com.trailequip.trail.application.service.TrailExportService;
import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.repository.TrailRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for OSM trail integration and export.
 * Provides GeoJSON, GPX export and trail ingestion management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/osm/trails")
@Tag(name = "OSM Trails", description = "OpenStreetMap trail integration and export")
@RequiredArgsConstructor
public class OSMTrailController {

    private final OSMIngestionService osmIngestionService;
    private final TrailExportService trailExportService;
    private final TrailRepository trailRepository;

    // ===== INGESTION ENDPOINTS =====

    /**
     * Ingest all hiking trails from Bucegi Mountains.
     * POST /api/v1/osm/trails/ingest/bucegi
     */
    @PostMapping("/ingest/bucegi")
    @Operation(summary = "Ingest trails from Bucegi Mountains")
    public ResponseEntity<OSMIngestionService.IngestionResult> ingestBucegiTrails() {
        log.info("Initiating Bucegi trail ingestion");
        OSMIngestionService.IngestionResult result = osmIngestionService.ingestBucegiTrails();
        return ResponseEntity.ok(result);
    }

    /**
     * Ingest trails from specific bounding box.
     * POST /api/v1/osm/trails/ingest/bbox?south=45.2&west=25.4&north=45.5&east=25.7
     */
    @PostMapping("/ingest/bbox")
    @Operation(summary = "Ingest trails by geographic bounding box")
    public ResponseEntity<OSMIngestionService.IngestionResult> ingestTrailsByBbox(
            @RequestParam double south,
            @RequestParam double west,
            @RequestParam double north,
            @RequestParam double east) {

        log.info("Ingesting trails by bbox: ({},{})-({},{})", south, west, north, east);
        OSMIngestionService.IngestionResult result = osmIngestionService.ingestTrailsByBbox(south, west, north, east);
        return ResponseEntity.ok(result);
    }

    /**
     * Ingest a single trail by OSM relation ID.
     * POST /api/v1/osm/trails/ingest/{osmRelationId}
     */
    @PostMapping("/ingest/{osmRelationId}")
    @Operation(summary = "Ingest single trail by OSM relation ID")
    public ResponseEntity<Trail> ingestTrailById(@PathVariable Long osmRelationId) {
        log.info("Ingesting trail by OSM ID: {}", osmRelationId);
        try {
            Trail trail = osmIngestionService.ingestTrailById(osmRelationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(trail);
        } catch (Exception e) {
            log.error("Failed to ingest trail: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ingest trails near a coordinate.
     * POST /api/v1/osm/trails/ingest/nearby?latitude=45.35&longitude=25.55&radius=10
     */
    @PostMapping("/ingest/nearby")
    @Operation(summary = "Ingest trails near a coordinate")
    public ResponseEntity<OSMIngestionService.IngestionResult> ingestTrailsNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radius) {

        log.info("Ingesting trails near ({}, {}) within {} km", latitude, longitude, radius);
        OSMIngestionService.IngestionResult result =
                osmIngestionService.ingestTrailsNearby(latitude, longitude, radius);
        return ResponseEntity.ok(result);
    }

    // ===== EXPORT ENDPOINTS =====

    /**
     * Export trail as GeoJSON.
     * GET /api/v1/osm/trails/{id}/geojson
     * For mapping in Leaflet, MapBox, etc.
     */
    @GetMapping("/{id}/geojson")
    @Operation(summary = "Export trail as GeoJSON")
    public ResponseEntity<String> exportTrailAsGeoJSON(@PathVariable UUID id) {
        Optional<Trail> trail = trailRepository.findById(id);

        if (trail.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String geoJson = trailExportService.exportAsGeoJSON(trail.get());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"trail-" + id + ".geojson\"")
                    .body(geoJson);
        } catch (Exception e) {
            log.error("Failed to export trail as GeoJSON: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export trail as GPX 1.1 format.
     * GET /api/v1/osm/trails/{id}/gpx
     * For GPS devices and mapping apps.
     */
    @GetMapping("/{id}/gpx")
    @Operation(summary = "Export trail as GPX 1.1")
    public ResponseEntity<String> exportTrailAsGPX(@PathVariable UUID id) {
        Optional<Trail> trail = trailRepository.findById(id);

        if (trail.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String gpx = trailExportService.exportAsGPX(trail.get());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"trail-" + id + ".gpx\"")
                    .body(gpx);
        } catch (Exception e) {
            log.error("Failed to export trail as GPX: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export all trails as GeoJSON FeatureCollection.
     * GET /api/v1/osm/trails/all/geojson?difficulty=HARD&source=openstreetmap
     */
    @GetMapping("/all/geojson")
    @Operation(summary = "Export all trails as GeoJSON FeatureCollection")
    public ResponseEntity<String> exportAllTrailsAsGeoJSON(
            @RequestParam(required = false) String difficulty, @RequestParam(required = false) String source) {

        try {
            List<Trail> trails;

            if (difficulty != null) {
                try {
                    Difficulty difficultyEnum = Difficulty.valueOf(difficulty.toUpperCase());
                    trails = trailRepository.findByDifficulty(difficultyEnum);
                } catch (IllegalArgumentException e) {
                    trails = List.of();
                }
            } else if (source != null) {
                trails = trailRepository.findBySource(source);
            } else {
                trails = trailRepository.findAll();
            }

            String geoJson = trailExportService.exportAsGeoJSONCollection(trails);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"trails.geojson\"")
                    .body(geoJson);
        } catch (Exception e) {
            log.error("Failed to export trails as GeoJSON: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== SEARCH & FILTER ENDPOINTS =====

    /**
     * Search trails by name.
     * GET /api/v1/osm/trails/search?q=Bucegi
     */
    @GetMapping("/search")
    @Operation(summary = "Search trails by name")
    public ResponseEntity<List<Trail>> searchTrails(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // This would require a full-text search implementation in the repository
        // For now, return filtered results
        List<Trail> trails = trailRepository.findAll().stream()
                .filter(t -> t.getName().toLowerCase().contains(q.toLowerCase()))
                .toList();

        return ResponseEntity.ok(trails);
    }

    /**
     * Get trails by data source.
     * GET /api/v1/osm/trails/source/{source}
     * Examples: "openstreetmap", "muntii-nostri.ro"
     */
    @GetMapping("/source/{source}")
    @Operation(summary = "Get trails by data source")
    public ResponseEntity<List<Trail>> getTrailsBySource(@PathVariable String source) {
        List<Trail> trails = trailRepository.findBySource(source);
        return ResponseEntity.ok(trails);
    }

    /**
     * Get OSM trail by relation ID.
     * GET /api/v1/osm/trails/osm-id/{osmId}
     */
    @GetMapping("/osm-id/{osmId}")
    @Operation(summary = "Get trail by OSM relation ID")
    public ResponseEntity<Trail> getTrailByOsmId(@PathVariable Long osmId) {
        Optional<Trail> trail = trailRepository.findByOsmId(osmId);
        return trail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Health check for OSM integration.
     * GET /api/v1/osm/trails/health
     */
    @GetMapping("/health")
    @Operation(summary = "Check OSM integration health")
    public ResponseEntity<HealthStatus> healthCheck() {
        long trailCount = trailRepository.count();
        long osmTrailCount = trailRepository.findBySource("openstreetmap").size();

        HealthStatus status = new HealthStatus("OSM Integration", "UP", trailCount, osmTrailCount);

        return ResponseEntity.ok(status);
    }

    /**
     * Health status response object.
     */
    public static class HealthStatus {
        public final String service;
        public final String status;
        public final long totalTrails;
        public final long osmTrails;

        public HealthStatus(String service, String status, long totalTrails, long osmTrails) {
            this.service = service;
            this.status = status;
            this.totalTrails = totalTrails;
            this.osmTrails = osmTrails;
        }
    }
}
