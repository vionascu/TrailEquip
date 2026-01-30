package com.trailequip.trail.domain.model;

/**
 * Difficulty enum for hiking trails, aligned with OSM hiking route standards.
 * Classification based on terrain, exposure, and technical requirements.
 */
public enum Difficulty {
    EASY(
        "Easy - Minimal elevation, well-maintained paths",
        10.0,      // maxSlopeThreshold (%)
        500,       // maxElevationGainThreshold (m)
        "🟢",      // emoji
        "Suitable for families and beginners. Paved or well-maintained forest paths."
    ),

    MEDIUM(
        "Moderate - Some elevation, occasional rocky sections",
        20.0,
        1500,
        "🟡",
        "Requires basic fitness. Mix of paths and rocky terrain with some exposure."
    ),

    HARD(
        "Hard - Significant elevation, exposed terrain",
        30.0,
        2500,
        "🔴",
        "Experienced hikers only. Steep terrain, exposed ridges, scrambling sections."
    ),

    ALPINE(
        "Alpine - High altitude, thin air, exposed ridges",
        40.0,
        3000,
        "🟣",
        "Alpine/mountaineering experience required. High altitude, weather exposure, thin air."
    ),

    SCRAMBLING(
        "Scrambling - Hands required, technical terrain",
        50.0,
        3500,
        "🧗",
        "Rock climbing skills required. Hands-on scrambling, exposed drops, technical moves."
    );

    private final String description;
    private final Double maxSlopeThreshold;      // Maximum slope percentage
    private final Integer maxElevationGainThreshold;  // Maximum elevation gain in meters
    private final String emoji;
    private final String fullDescription;

    Difficulty(String description, Double maxSlopeThreshold, Integer maxElevationGainThreshold,
               String emoji, String fullDescription) {
        this.description = description;
        this.maxSlopeThreshold = maxSlopeThreshold;
        this.maxElevationGainThreshold = maxElevationGainThreshold;
        this.emoji = emoji;
        this.fullDescription = fullDescription;
    }

    // ===== GETTERS =====

    public String getDescription() {
        return description;
    }

    public Double getMaxSlopeThreshold() {
        return maxSlopeThreshold;
    }

    public Integer getMaxElevationGainThreshold() {
        return maxElevationGainThreshold;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    /**
     * Infer difficulty from trail metrics.
     * Used when OSM data doesn't explicitly provide difficulty.
     *
     * @param elevationGain elevation in meters
     * @param maxSlope maximum slope in percentage
     * @return inferred Difficulty
     */
    public static Difficulty inferFromMetrics(Integer elevationGain, Double maxSlope) {
        if (elevationGain == null || maxSlope == null) {
            return MEDIUM;  // Default to medium if data missing
        }

        // Prioritize slope over elevation for technical difficulty
        if (maxSlope > SCRAMBLING.maxSlopeThreshold) {
            return SCRAMBLING;
        }
        if (maxSlope > ALPINE.maxSlopeThreshold) {
            return ALPINE;
        }
        if (maxSlope > HARD.maxSlopeThreshold) {
            return HARD;
        }

        // Then check elevation
        if (elevationGain > ALPINE.maxElevationGainThreshold) {
            return ALPINE;
        }
        if (elevationGain > HARD.maxElevationGainThreshold) {
            return HARD;
        }
        if (elevationGain > MEDIUM.maxElevationGainThreshold) {
            return MEDIUM;
        }

        return EASY;
    }

    /**
     * Check if trail meets difficulty criteria.
     *
     * @param elevationGain elevation in meters
     * @param maxSlope maximum slope in percentage
     * @return true if metrics match this difficulty level
     */
    public boolean matches(Integer elevationGain, Double maxSlope) {
        if (elevationGain == null || maxSlope == null) {
            return false;
        }

        return elevationGain <= this.maxElevationGainThreshold &&
               maxSlope <= this.maxSlopeThreshold;
    }
}
