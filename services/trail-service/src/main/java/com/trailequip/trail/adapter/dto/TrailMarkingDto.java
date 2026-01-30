package com.trailequip.trail.adapter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for TrailMarking.
 * Represents OSMC trail marking symbols.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrailMarkingDto {

    private Long id;
    private String osmcSymbol;
    private String color;
    private String shape;
    private String hexColor;
    private String description;

    /**
     * Convert from domain TrailMarking to DTO.
     */
    public static TrailMarkingDto fromDomain(com.trailequip.trail.domain.model.TrailMarking marking) {
        if (marking == null) {
            return null;
        }

        return TrailMarkingDto.builder()
                .id(marking.getId())
                .osmcSymbol(marking.getOsmcSymbol())
                .color(marking.getColor() != null ? marking.getColor().name() : null)
                .shape(marking.getShape() != null ? marking.getShape().name() : null)
                .hexColor(marking.getHexColor())
                .description(marking.getDescription())
                .build();
    }
}
