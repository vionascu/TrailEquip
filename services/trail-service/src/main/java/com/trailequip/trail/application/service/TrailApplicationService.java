package com.trailequip.trail.application.service;

import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.repository.TrailRepository;
import com.trailequip.trail.domain.service.DifficultyClassifier;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TrailApplicationService {

    private final TrailRepository trailRepository;
    private final DifficultyClassifier difficultyClassifier;

    public TrailApplicationService(TrailRepository trailRepository, DifficultyClassifier difficultyClassifier) {
        this.trailRepository = trailRepository;
        this.difficultyClassifier = difficultyClassifier;
    }

    public Trail createTrail(Trail trail) {
        if (trail.getDifficulty() == null) {
            trail.setDifficulty(difficultyClassifier.classify(trail));
        }
        return trailRepository.save(trail);
    }

    public Optional<Trail> getTrail(UUID id) {
        return trailRepository.findById(id);
    }

    public List<Trail> getAllTrails() {
        return trailRepository.findAll();
    }

    public List<Trail> getTrailsByDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isEmpty()) {
            return trailRepository.findAll();
        }
        try {
            Difficulty difficultyEnum = Difficulty.valueOf(difficulty.toUpperCase());
            return trailRepository.findByDifficulty(difficultyEnum);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public List<Trail> suggestTrailsInArea(double centerLat, double centerLon, double radiusKm, String difficulty) {
        // Note: Geographic filtering with centerLat/centerLon/radiusKm requires PostGIS
        // For now, only filtering by difficulty is implemented
        if (difficulty == null || difficulty.isEmpty()) {
            return trailRepository.findAll();
        }
        try {
            Difficulty difficultyEnum = Difficulty.valueOf(difficulty.toUpperCase());
            return trailRepository.findTrailsInArea(difficultyEnum);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public Trail updateTrail(Trail trail) {
        Optional<Trail> existing = trailRepository.findById(trail.getId());
        if (existing.isPresent()) {
            Trail toUpdate = existing.get();
            if (trail.getName() != null) toUpdate.setName(trail.getName());
            if (trail.getDescription() != null) toUpdate.setDescription(trail.getDescription());
            if (trail.getDifficulty() != null) toUpdate.setDifficulty(trail.getDifficulty());
            if (trail.getDistance() != null && trail.getDistance() > 0) toUpdate.setDistance(trail.getDistance());
            if (trail.getElevationGain() != null) toUpdate.setElevationGain(trail.getElevationGain());
            if (trail.getElevationLoss() != null) toUpdate.setElevationLoss(trail.getElevationLoss());
            if (trail.getMaxSlope() != null && trail.getMaxSlope() >= 0) toUpdate.setMaxSlope(trail.getMaxSlope());
            if (trail.getDurationMinutes() != null) toUpdate.setDurationMinutes(trail.getDurationMinutes());
            if (trail.getAvgSlope() != null) toUpdate.setAvgSlope(trail.getAvgSlope());
            if (trail.getTerrain() != null) toUpdate.setTerrain(trail.getTerrain());
            if (trail.getHazards() != null) toUpdate.setHazards(trail.getHazards());
            if (trail.getMarking() != null) toUpdate.setMarking(trail.getMarking());
            toUpdate.setUpdatedAt(Instant.now());
            return trailRepository.save(toUpdate);
        }
        return trailRepository.save(trail);
    }

    public void deleteTrail(UUID id) {
        trailRepository.deleteById(id);
    }
}
