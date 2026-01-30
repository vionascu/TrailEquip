package com.trailequip.trail.adapter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for creating a new trail.
 * Used in POST requests to create trails manually or import from external sources.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateTrailDto {

    private Long osmId;
    private String name;
    private String description;
    private String ref;

    private Double distance;
    private Integer elevationGain;
    private Integer elevationLoss;
    private Integer durationMinutes;
    private Double maxSlope;
    private Double avgSlope;
    private Integer maxElevation;

    private List<String> terrain;
    private String difficulty;
    private List<String> hazards;

    private String source;
    private String osmcSymbol;

    /**
     * Validate required fields.
     */
    public void validate() throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Trail name is required");
        }

        if (distance == null || distance <= 0) {
            throw new ValidationException("Trail distance must be positive");
        }

        if (difficulty == null || difficulty.trim().isEmpty()) {
            throw new ValidationException("Trail difficulty is required");
        }
    }

    /**
     * Convert to domain Trail object.
     */
    public com.trailequip.trail.domain.model.Trail toDomain() {
        validate();

        com.trailequip.trail.domain.model.Trail trail = new com.trailequip.trail.domain.model.Trail();
        trail.setOsmId(osmId);
        trail.setName(name);
        trail.setDescription(description);
        trail.setRef(ref);
        trail.setDistance(distance);
        trail.setElevationGain(elevationGain);
        trail.setElevationLoss(elevationLoss);
        trail.setDurationMinutes(durationMinutes);
        trail.setMaxSlope(maxSlope);
        trail.setAvgSlope(avgSlope);
        trail.setMaxElevation(maxElevation);
        trail.setTerrain(terrain);
        trail.setHazards(hazards);
        trail.setSource(source != null ? source : "manual");

        try {
            trail.setDifficulty(com.trailequip.trail.domain.model.Difficulty.valueOf(difficulty.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid difficulty: " + difficulty);
        }

        return trail;
    }

    /**
     * Validation exception for DTOs.
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
