package com.trailequip.trail.application.service;

import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.repository.TrailRepository;
import com.trailequip.trail.domain.service.DifficultyClassifier;
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
        return trailRepository.findByDifficulty(difficulty);
    }

    public List<Trail> suggestTrailsInArea(double centerLat, double centerLon, double radiusKm, String difficulty) {
        // Note: Geographic filtering with centerLat/centerLon/radiusKm requires PostGIS
        // For now, only filtering by difficulty is implemented
        return trailRepository.findTrailsInArea(difficulty);
    }

    public void deleteTrail(UUID id) {
        trailRepository.deleteById(id);
    }
}
