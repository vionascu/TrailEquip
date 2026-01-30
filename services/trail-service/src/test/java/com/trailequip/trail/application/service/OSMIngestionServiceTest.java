package com.trailequip.trail.application.service;

import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.repository.TrailRepository;
import com.trailequip.trail.infrastructure.overpass.OverpassApiClient;
import com.trailequip.trail.infrastructure.overpass.OverpassRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OSMIngestionService.
 * Tests trail ingestion pipeline including fetch, normalize, validate, and persist.
 */
@ExtendWith(MockitoExtension.class)
class OSMIngestionServiceTest {

    @Mock
    private OverpassApiClient overpassApiClient;

    @Mock
    private TrailRepository trailRepository;

    private TrailNormalizer trailNormalizer;
    private OSMIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        trailNormalizer = new TrailNormalizer();
        ingestionService = new OSMIngestionService(overpassApiClient, trailNormalizer, trailRepository);
    }

    @Test
    void shouldIngestBucegiTrailsSuccessfully() {
        List<OverpassRelation> mockRelations = createMockRelations(3);
        when(overpassApiClient.queryBucegiHikingRoutes()).thenReturn(mockRelations);
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trailRepository.findByOsmId(any())).thenReturn(Optional.empty());

        OSMIngestionService.IngestionResult result = ingestionService.ingestBucegiTrails();

        assertTrue(result.isSuccess());
        assertEquals(3, result.getFetched());
        assertTrue(result.getNormalized() > 0);
        verify(overpassApiClient, times(1)).queryBucegiHikingRoutes();
        verify(trailRepository, atLeastOnce()).save(any(Trail.class));
    }

    @Test
    void shouldIngestTrailsByBoundingBox() {
        List<OverpassRelation> mockRelations = createMockRelations(2);
        when(overpassApiClient.queryHikingRoutesByBbox(45.2, 25.4, 45.5, 25.7))
            .thenReturn(mockRelations);
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trailRepository.findByOsmId(any())).thenReturn(Optional.empty());

        OSMIngestionService.IngestionResult result =
            ingestionService.ingestTrailsByBbox(45.2, 25.4, 45.5, 25.7);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getFetched());
        verify(overpassApiClient, times(1))
            .queryHikingRoutesByBbox(45.2, 25.4, 45.5, 25.7);
    }

    @Test
    void shouldIngestSingleTrailById() {
        OverpassRelation mockRelation = createMockRelation(123L);
        when(overpassApiClient.queryTrailById(123L)).thenReturn(mockRelation);
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trail trail = ingestionService.ingestTrailById(123L);

        assertNotNull(trail);
        assertEquals(123L, trail.getOsmId());
        verify(trailRepository, times(1)).save(any(Trail.class));
    }

    @Test
    void shouldIngestTrailsNearby() {
        List<OverpassRelation> mockRelations = createMockRelations(2);
        when(overpassApiClient.queryTrailsNearby(45.35, 25.54, 10))
            .thenReturn(mockRelations);
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trailRepository.findByOsmId(any())).thenReturn(Optional.empty());

        OSMIngestionService.IngestionResult result =
            ingestionService.ingestTrailsNearby(45.35, 25.54, 10);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getFetched());
    }

    @Test
    void shouldDeduplicateTrailsByOsmId() {
        List<OverpassRelation> mockRelations = new ArrayList<>();
        // Add same relation twice
        mockRelations.add(createMockRelation(123L));
        mockRelations.add(createMockRelation(123L));

        when(overpassApiClient.queryBucegiHikingRoutes()).thenReturn(mockRelations);
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trailRepository.findByOsmId(any())).thenReturn(Optional.empty());

        OSMIngestionService.IngestionResult result = ingestionService.ingestBucegiTrails();

        // Should deduplicate to 1
        assertEquals(1, result.getDeduplicated());
    }

    @Test
    void shouldUpdateExistingTrailsFromNewerVersion() {
        OverpassRelation mockRelation = createMockRelation(123L);
        mockRelation = new OverpassRelation(
            123L, "Updated Trail Name", "hiking", "01MN", null, null,
            "blue:blue_stripe", null, "Updated description",
            new ArrayList<>(), mockRelation.getCoordinates()
        );

        Trail existingTrail = new Trail();
        existingTrail.setOsmId(123L);
        existingTrail.setName("Old Trail Name");

        when(overpassApiClient.queryTrailById(123L)).thenReturn(mockRelation);
        when(trailRepository.findByOsmId(123L)).thenReturn(Optional.of(existingTrail));
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ingestionService.ingestTrailById(123L);

        verify(trailRepository, times(1)).save(argThat(t ->
            "Updated Trail Name".equals(t.getName())
        ));
    }

    @Test
    void shouldValidateRequiredFields() {
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(25.54, 45.35, 1000));

        OverpassRelation invalidRelation = new OverpassRelation(
            123L, null, "hiking", null, null, null,  // null name
            null, null, null, new ArrayList<>(), coords
        );

        when(overpassApiClient.queryBucegiHikingRoutes()).thenReturn(List.of(invalidRelation));

        OSMIngestionService.IngestionResult result = ingestionService.ingestBucegiTrails();

        assertTrue(result.getFailed() > 0);
    }

    @Test
    void shouldCountCreatedAndUpdatedTrails() {
        Trail existingTrail = new Trail();
        existingTrail.setOsmId(100L);

        when(overpassApiClient.queryBucegiHikingRoutes())
            .thenReturn(List.of(createMockRelation(100L), createMockRelation(101L)));
        when(trailRepository.findByOsmId(100L)).thenReturn(Optional.of(existingTrail));
        when(trailRepository.findByOsmId(101L)).thenReturn(Optional.empty());
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OSMIngestionService.IngestionResult result = ingestionService.ingestBucegiTrails();

        assertEquals(1, result.getCreated());
        assertEquals(1, result.getUpdated());
    }

    @Test
    void shouldHandleAPIErrors() {
        when(overpassApiClient.queryBucegiHikingRoutes())
            .thenThrow(new RuntimeException("API Error"));

        OSMIngestionService.IngestionResult result = ingestionService.ingestBucegiTrails();

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void shouldNormalizeTrailsBeforePersistence() {
        OverpassRelation mockRelation = createMockRelation(123L);
        when(overpassApiClient.queryBucegiHikingRoutes()).thenReturn(List.of(mockRelation));
        when(trailRepository.save(any(Trail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trailRepository.findByOsmId(any())).thenReturn(Optional.empty());

        OSMIngestionService.IngestionResult result = ingestionService.ingestBucegiTrails();

        verify(trailRepository, times(1)).save(argThat(trail ->
            trail.getName() != null &&
            trail.getDistance() != null &&
            trail.getDifficulty() != null &&
            trail.getGeometry() != null
        ));
    }

    // Helper methods

    private List<OverpassRelation> createMockRelations(int count) {
        List<OverpassRelation> relations = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            relations.add(createMockRelation(100L + i));
        }
        return relations;
    }

    private OverpassRelation createMockRelation(long id) {
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(25.54, 45.35, 1000));
        coords.add(new Coordinate(25.541, 45.351, 1100));
        coords.add(new Coordinate(25.542, 45.352, 1200));

        return new OverpassRelation(
            id,
            "Test Trail " + id,
            "hiking",
            "01MN",
            "lwn",
            "OpenStreetMap",
            "blue:blue_stripe",
            "moderate",
            "A test trail",
            new ArrayList<>(),
            coords
        );
    }
}
