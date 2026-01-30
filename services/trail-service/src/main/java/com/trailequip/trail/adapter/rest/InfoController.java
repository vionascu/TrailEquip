package com.trailequip.trail.adapter.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Info", description = "API information and documentation")
public class InfoController {

    private final Optional<BuildProperties> buildProperties;

    public InfoController(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/")
    @Operation(summary = "API welcome and documentation")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("service", "TrailEquip Trail Service");
        info.put("version", buildProperties.map(BuildProperties::getVersion).orElse("0.1.0-SNAPSHOT"));
        info.put("description", "REST API for discovering, planning, and outfitting hiking trails");
        info.put("status", "running");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("API Endpoints", "/api/v1/trails");
        endpoints.put("Health Check", "/actuator/health");
        endpoints.put("Metrics", "/actuator/metrics");
        endpoints.put("API Docs", "/swagger-ui.html");
        endpoints.put("API Schema", "/v3/api-docs");

        info.put("endpoints", endpoints);

        Map<String, String> features = new LinkedHashMap<>();
        features.put("Trail Management", "List, search, and retrieve trail information");
        features.put("OSM Integration", "OpenStreetMap trail data integration (coming soon)");
        features.put("Trail Export", "Export trails as GeoJSON or GPX formats");
        features.put("Difficulty Classification", "Automatic difficulty inference from terrain metrics");

        info.put("features", features);

        return ResponseEntity.ok(info);
    }
}
