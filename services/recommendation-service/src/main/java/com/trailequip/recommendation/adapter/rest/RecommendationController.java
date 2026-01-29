package com.trailequip.recommendation.adapter.rest;

import com.trailequip.recommendation.application.service.EquipmentRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "Equipment recommendations")
public class RecommendationController {

    private final EquipmentRecommendationService equipmentService;

    public RecommendationController(EquipmentRecommendationService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping("/equipment")
    @Operation(summary = "Get equipment recommendations")
    public ResponseEntity<Map<String, Object>> getEquipmentRecommendations(
            @RequestBody Map<String, String> request) {
        String trailId = request.get("trailId");
        String forecastStart = request.get("forecastStart");
        String forecastEnd = request.get("forecastEnd");

        Map<String, Object> recommendations =
                equipmentService.recommend(UUID.fromString(trailId), forecastStart, forecastEnd);

        return ResponseEntity.ok(recommendations);
    }
}
