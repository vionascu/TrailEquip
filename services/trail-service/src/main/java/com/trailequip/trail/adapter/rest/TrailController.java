package com.trailequip.trail.adapter.rest;

import com.trailequip.trail.application.service.TrailApplicationService;
import com.trailequip.trail.domain.model.Trail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trails")
@Tag(name = "Trails", description = "Trail management and browsing")
@CrossOrigin(
        origins = "http://localhost:3001",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
public class TrailController {

    private final TrailApplicationService trailApplicationService;

    public TrailController(TrailApplicationService trailApplicationService) {
        this.trailApplicationService = trailApplicationService;
    }

    @GetMapping
    @Operation(summary = "List all trails")
    public ResponseEntity<List<Trail>> getAllTrails(@RequestParam(required = false) String difficulty) {
        if (difficulty != null) {
            List<Trail> trails = trailApplicationService.getTrailsByDifficulty(difficulty);
            return ResponseEntity.ok(trails);
        }
        List<Trail> trails = trailApplicationService.getAllTrails();
        return ResponseEntity.ok(trails);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trail by ID")
    public ResponseEntity<Trail> getTrail(@PathVariable UUID id) {
        Optional<Trail> trail = trailApplicationService.getTrail(id);
        return trail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new trail")
    public ResponseEntity<Trail> createTrail(@RequestBody Trail trail) {
        Trail created = trailApplicationService.createTrail(trail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/suggest")
    @Operation(summary = "Suggest trails in area")
    public ResponseEntity<List<Trail>> suggestTrails(
            @RequestParam double centerLat,
            @RequestParam double centerLon,
            @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(required = false) String difficulty) {
        List<Trail> suggestions =
                trailApplicationService.suggestTrailsInArea(centerLat, centerLon, radiusKm, difficulty);
        return ResponseEntity.ok(suggestions);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trail")
    public ResponseEntity<Void> deleteTrail(@PathVariable UUID id) {
        trailApplicationService.deleteTrail(id);
        return ResponseEntity.noContent().build();
    }
}
