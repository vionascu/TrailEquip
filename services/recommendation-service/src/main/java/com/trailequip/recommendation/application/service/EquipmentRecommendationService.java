package com.trailequip.recommendation.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EquipmentRecommendationService {

    public Map<String, Object> recommend(UUID trailId, String forecastStart, String forecastEnd) {
        List<Map<String, Object>> equipment = new ArrayList<>();

        equipment.add(Map.of(
            "category", "LAYERS",
            "items", new Object[]{
                Map.of("name", "Base Layer (Thermal)", "reason", "Expected temp 2-5°C")
            }
        ));

        equipment.add(Map.of(
            "category", "OUTERWEAR",
            "items", new Object[]{
                Map.of("name", "Rain Shell", "reason", "40-50% precipitation chance")
            }
        ));

        equipment.add(Map.of(
            "category", "TRACTION",
            "items", new Object[]{
                Map.of("name", "Microspikes", "reason", "Low temp + precipitation = ice risk")
            }
        ));

        return Map.of(
            "equipment", equipment,
            "warnings", new String[]{"High wind expected on ridges"},
            "summary", "Moderate conditions; bring layered system and rain protection"
        );
    }
}
