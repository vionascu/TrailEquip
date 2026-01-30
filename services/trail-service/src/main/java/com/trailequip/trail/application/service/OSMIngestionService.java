package com.trailequip.trail.application.service;

import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.repository.TrailRepository;
import com.trailequip.trail.infrastructure.overpass.OverpassApiClient;
import com.trailequip.trail.infrastructure.overpass.OverpassRelation;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for ingesting hiking trails from OpenStreetMap via Overpass API.
 * Handles fetching, normalizing, deduplication, and persistence of trail data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OSMIngestionService {

    private final OverpassApiClient overpassApiClient;
    private final TrailNormalizer trailNormalizer;
    private final TrailRepository trailRepository;

    /**
     * Ingest all hiking trails from Bucegi Mountains region.
     * This is the main entry point for trail ingestion.
     *
     * @return IngestionResult with statistics
     */
    @Transactional
    public IngestionResult ingestBucegiTrails() {
        log.info("Starting OSM trail ingestion for Bucegi Mountains");

        IngestionResult result = new IngestionResult();
        try {
            // Fetch trails from Overpass API
            List<OverpassRelation> relations = overpassApiClient.queryBucegiHikingRoutes();
            result.setFetched(relations.size());
            log.info("Fetched {} hiking relations from Overpass API", relations.size());

            // Normalize to domain objects
            List<Trail> trails = relations.stream()
                .map(relation -> {
                    try {
                        return trailNormalizer.normalizeToDomain(relation);
                    } catch (Exception e) {
                        log.warn("Failed to normalize trail {}: {}", relation.getId(), e.getMessage());
                        result.incrementFailed();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            result.setNormalized(trails.size());
            log.info("Normalized {} trails to domain objects", trails.size());

            // Deduplicate by OSM ID
            trails = deduplicateByOsmId(trails);
            result.setDeduplicated(trails.size());
            log.info("Deduplicated to {} unique trails", trails.size());

            // Validate trail data
            trails = validateTrails(trails, result);
            log.info("Validated {} trails", trails.size());

            // Persist to database
            List<Trail> saved = persistTrails(trails, result);
            log.info("Persisted {} trails to database", saved.size());

            result.setSuccess(true);
        } catch (Exception e) {
            log.error("OSM trail ingestion failed", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        log.info("OSM trail ingestion completed: {}", result);
        return result;
    }

    /**
     * Ingest trails from a specific region by bounding box.
     */
    @Transactional
    public IngestionResult ingestTrailsByBbox(double south, double west, double north, double east) {
        log.info("Ingesting trails in region: south={}, west={}, north={}, east={}", south, west, north, east);

        IngestionResult result = new IngestionResult();
        try {
            List<OverpassRelation> relations = overpassApiClient.queryHikingRoutesByBbox(south, west, north, east);
            result.setFetched(relations.size());

            List<Trail> trails = relations.stream()
                .map(r -> {
                    try {
                        return trailNormalizer.normalizeToDomain(r);
                    } catch (Exception e) {
                        log.warn("Failed to normalize trail: {}", e.getMessage());
                        result.incrementFailed();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            trails = deduplicateByOsmId(trails);
            trails = validateTrails(trails, result);
            persistTrails(trails, result);

            result.setSuccess(true);
        } catch (Exception e) {
            log.error("Trail ingestion by bbox failed", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * Ingest a single trail by OSM relation ID.
     */
    @Transactional
    public Trail ingestTrailById(Long osmRelationId) {
        log.info("Ingesting trail with OSM ID: {}", osmRelationId);

        try {
            OverpassRelation relation = overpassApiClient.queryTrailById(osmRelationId);
            if (relation == null) {
                throw new IllegalArgumentException("Trail not found in OSM: " + osmRelationId);
            }

            Trail trail = trailNormalizer.normalizeToDomain(relation);
            trail = validateTrail(trail);

            return trailRepository.save(trail);
        } catch (Exception e) {
            log.error("Failed to ingest trail {}: {}", osmRelationId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Find and ingest trails near a coordinate.
     */
    @Transactional
    public IngestionResult ingestTrailsNearby(double latitude, double longitude, double radiusKm) {
        log.info("Ingesting trails near ({}, {}) within {} km", latitude, longitude, radiusKm);

        IngestionResult result = new IngestionResult();
        try {
            List<OverpassRelation> relations = overpassApiClient.queryTrailsNearby(latitude, longitude, radiusKm);
            result.setFetched(relations.size());

            List<Trail> trails = relations.stream()
                .map(r -> {
                    try {
                        return trailNormalizer.normalizeToDomain(r);
                    } catch (Exception e) {
                        result.incrementFailed();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            trails = deduplicateByOsmId(trails);
            trails = validateTrails(trails, result);
            persistTrails(trails, result);

            result.setSuccess(true);
        } catch (Exception e) {
            log.error("Trail ingestion nearby failed", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * Deduplicate trails by OSM ID.
     * Newer/updated versions replace older ones.
     */
    private List<Trail> deduplicateByOsmId(List<Trail> trails) {
        Map<Long, Trail> uniqueTrails = new LinkedHashMap<>();

        for (Trail trail : trails) {
            if (trail.getOsmId() != null) {
                uniqueTrails.put(trail.getOsmId(), trail);
            }
        }

        return new ArrayList<>(uniqueTrails.values());
    }

    /**
     * Validate all trails before persistence.
     */
    private List<Trail> validateTrails(List<Trail> trails, IngestionResult result) {
        return trails.stream()
            .filter(trail -> {
                try {
                    validateTrail(trail);
                    return true;
                } catch (ValidationException e) {
                    log.warn("Trail validation failed {}: {}", trail.getOsmId(), e.getMessage());
                    result.incrementFailed();
                    return false;
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Validate a single trail's data integrity.
     */
    private Trail validateTrail(Trail trail) {
        if (trail.getName() == null || trail.getName().trim().isEmpty()) {
            throw new ValidationException("Trail name is required");
        }

        if (trail.getDistance() == null || trail.getDistance() <= 0) {
            throw new ValidationException("Trail distance must be positive");
        }

        if (trail.getGeometry() == null || trail.getGeometry().isEmpty()) {
            throw new ValidationException("Trail geometry is required");
        }

        if (trail.getDifficulty() == null) {
            throw new ValidationException("Trail difficulty is required");
        }

        return trail;
    }

    /**
     * Persist trails to database, handling duplicates.
     */
    private List<Trail> persistTrails(List<Trail> trails, IngestionResult result) {
        List<Trail> saved = new ArrayList<>();

        for (Trail trail : trails) {
            try {
                // Check if trail already exists by OSM ID
                if (trail.getOsmId() != null) {
                    Optional<Trail> existing = trailRepository.findByOsmId(trail.getOsmId());

                    if (existing.isPresent()) {
                        Trail existingTrail = existing.get();
                        updateTrailFromNew(existingTrail, trail);
                        saved.add(trailRepository.save(existingTrail));
                        result.incrementUpdated();
                    } else {
                        saved.add(trailRepository.save(trail));
                        result.incrementCreated();
                    }
                } else {
                    saved.add(trailRepository.save(trail));
                    result.incrementCreated();
                }
            } catch (Exception e) {
                log.error("Failed to persist trail {}: {}", trail.getOsmId(), e.getMessage());
                result.incrementFailed();
            }
        }

        return saved;
    }

    /**
     * Update existing trail with data from new OSM version.
     */
    private void updateTrailFromNew(Trail existing, Trail newTrail) {
        existing.setName(newTrail.getName());
        existing.setDescription(newTrail.getDescription());
        existing.setDistance(newTrail.getDistance());
        existing.setElevationGain(newTrail.getElevationGain());
        existing.setElevationLoss(newTrail.getElevationLoss());
        existing.setDurationMinutes(newTrail.getDurationMinutes());
        existing.setMaxSlope(newTrail.getMaxSlope());
        existing.setAvgSlope(newTrail.getAvgSlope());
        existing.setMaxElevation(newTrail.getMaxElevation());
        existing.setGeometry(newTrail.getGeometry());
        existing.setDifficulty(newTrail.getDifficulty());
        existing.setTerrain(newTrail.getTerrain());
        existing.setHazards(newTrail.getHazards());
        existing.setMarking(newTrail.getMarking());
        existing.setRef(newTrail.getRef());
    }

    /**
     * Async ingestion for background processing.
     */
    @Async
    public void ingestBucegiTrailsAsync() {
        ingestBucegiTrails();
    }

    /**
     * Result object containing ingestion statistics.
     */
    public static class IngestionResult {
        private boolean success;
        private int fetched;
        private int normalized;
        private int deduplicated;
        private int validated;
        private int created;
        private int updated;
        private int failed;
        private String errorMessage;

        public void incrementFailed() {
            this.failed++;
        }

        public void incrementCreated() {
            this.created++;
        }

        public void incrementUpdated() {
            this.updated++;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public int getFetched() { return fetched; }
        public void setFetched(int fetched) { this.fetched = fetched; }

        public int getNormalized() { return normalized; }
        public void setNormalized(int normalized) { this.normalized = normalized; }

        public int getDeduplicated() { return deduplicated; }
        public void setDeduplicated(int deduplicated) { this.deduplicated = deduplicated; }

        public int getValidated() { return validated; }
        public void setValidated(int validated) { this.validated = validated; }

        public int getCreated() { return created; }
        public void setCreated(int created) { this.created = created; }

        public int getUpdated() { return updated; }
        public void setUpdated(int updated) { this.updated = updated; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        @Override
        public String toString() {
            return "IngestionResult{" +
                "success=" + success +
                ", fetched=" + fetched +
                ", normalized=" + normalized +
                ", deduplicated=" + deduplicated +
                ", created=" + created +
                ", updated=" + updated +
                ", failed=" + failed +
                '}';
        }
    }

    /**
     * Custom validation exception.
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
