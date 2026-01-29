package com.trailequip.trail.domain.service;

import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import org.springframework.stereotype.Service;

@Service
public class DifficultyClassifier {

    public Difficulty classify(Trail trail) {
        if (hasRockClimbingTerrain(trail)) {
            return Difficulty.ROCK_CLIMBING;
        }
        if (isHard(trail)) {
            return Difficulty.HARD;
        }
        if (isMedium(trail)) {
            return Difficulty.MEDIUM;
        }
        if (isEasy(trail)) {
            return Difficulty.EASY;
        }
        return Difficulty.HARD;
    }

    private boolean hasRockClimbingTerrain(Trail trail) {
        if (trail.getTerrain() == null) return false;
        for (String terrain : trail.getTerrain()) {
            if (terrain.equalsIgnoreCase("scramble")
                    || terrain.equalsIgnoreCase("technical_climbing")
                    || terrain.equalsIgnoreCase("via_ferrata")) {
                return true;
            }
        }
        return trail.getMaxSlope() != null && trail.getMaxSlope() > 60;
    }

    private boolean isHard(Trail trail) {
        return trail.getDistance() != null
                && trail.getDistance() <= 50
                && trail.getElevationGain() != null
                && trail.getElevationGain() <= 2000
                && trail.getAvgSlope() != null
                && trail.getAvgSlope() <= 20
                && trail.getMaxSlope() != null
                && trail.getMaxSlope() <= 50;
    }

    private boolean isMedium(Trail trail) {
        return trail.getDistance() != null
                && trail.getDistance() <= 30
                && trail.getElevationGain() != null
                && trail.getElevationGain() <= 1200
                && trail.getAvgSlope() != null
                && trail.getAvgSlope() <= 15
                && trail.getMaxSlope() != null
                && trail.getMaxSlope() <= 30
                && !hasForbiddenTerrainForMedium(trail);
    }

    private boolean isEasy(Trail trail) {
        return trail.getDistance() != null
                && trail.getDistance() <= 15
                && trail.getElevationGain() != null
                && trail.getElevationGain() <= 500
                && trail.getAvgSlope() != null
                && trail.getAvgSlope() <= 10
                && !hasForbiddenTerrainForEasy(trail);
    }

    private boolean hasForbiddenTerrainForEasy(Trail trail) {
        if (trail.getTerrain() == null) return false;
        for (String terrain : trail.getTerrain()) {
            if (terrain.equalsIgnoreCase("scramble")
                    || terrain.equalsIgnoreCase("exposed_ridge")
                    || terrain.equalsIgnoreCase("via_ferrata")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasForbiddenTerrainForMedium(Trail trail) {
        if (trail.getTerrain() == null) return false;
        for (String terrain : trail.getTerrain()) {
            if (terrain.equalsIgnoreCase("via_ferrata")
                    || terrain.equalsIgnoreCase("technical_climbing")) {
                return true;
            }
        }
        return false;
    }
}
