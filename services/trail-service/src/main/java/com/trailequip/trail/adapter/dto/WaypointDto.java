package com.trailequip.trail.adapter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object for Waypoint.
 * Represents a specific point along a trail (peak, shelter, junction, etc.).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WaypointDto {

    private UUID id;
    private Long osmNodeId;
    private Integer sequenceOrder;

    private Double latitude;
    private Double longitude;
    private Integer elevation;

    private String name;
    private String type;
    private String description;

    /**
     * Convert from domain Waypoint to DTO.
     */
    public static WaypointDto fromDomain(com.trailequip.trail.domain.model.Waypoint waypoint) {
        if (waypoint == null) {
            return null;
        }

        return WaypointDto.builder()
                .id(waypoint.getId())
                .osmNodeId(waypoint.getOsmNodeId())
                .sequenceOrder(waypoint.getSequenceOrder())
                .latitude(waypoint.getLatitude())
                .longitude(waypoint.getLongitude())
                .elevation(waypoint.getElevation())
                .name(waypoint.getName())
                .type(waypoint.getType() != null ? waypoint.getType().name() : null)
                .description(waypoint.getDescription())
                .build();
    }
}
