package com.trailequip.trail.adapter.dto;

import com.trailequip.trail.domain.model.Difficulty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Trail API responses.
 * Simplified version without heavy relationships for API serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrailDto {

    private UUID id;
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
    private Difficulty difficulty;
    private List<String> hazards;

    private String source;
    private Instant createdAt;
    private Instant updatedAt;

    private TrailMarkingDto marking;
    private List<WaypointDto> waypoints;

    /**
     * Convert from domain Trail to DTO.
     */
    public static TrailDto fromDomain(com.trailequip.trail.domain.model.Trail trail) {
        return TrailDto.builder()
                .id(trail.getId())
                .osmId(trail.getOsmId())
                .name(trail.getName())
                .description(trail.getDescription())
                .ref(trail.getRef())
                .distance(trail.getDistance())
                .elevationGain(trail.getElevationGain())
                .elevationLoss(trail.getElevationLoss())
                .durationMinutes(trail.getDurationMinutes())
                .maxSlope(trail.getMaxSlope())
                .avgSlope(trail.getAvgSlope())
                .maxElevation(trail.getMaxElevation())
                .terrain(trail.getTerrain())
                .difficulty(trail.getDifficulty())
                .hazards(trail.getHazards())
                .source(trail.getSource())
                .createdAt(trail.getCreatedAt())
                .updatedAt(trail.getUpdatedAt())
                .marking(trail.getMarking() != null ? TrailMarkingDto.fromDomain(trail.getMarking()) : null)
                .waypoints(trail.getWaypoints() != null ?
                    trail.getWaypoints().stream()
                        .map(WaypointDto::fromDomain)
                        .toList()
                    : null)
                .build();
    }
}
