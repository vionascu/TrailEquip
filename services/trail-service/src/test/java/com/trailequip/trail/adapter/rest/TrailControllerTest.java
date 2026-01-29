package com.trailequip.trail.adapter.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trailequip.trail.application.service.TrailApplicationService;
import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrailController.class)
public class TrailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrailApplicationService trailApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Trail sampleTrail;
    private UUID trailId;

    @BeforeEach
    public void setup() {
        trailId = UUID.randomUUID();
        sampleTrail = new Trail(
                "Omu Peak Loop",
                "Classic route via alpine meadows and exposed ridge",
                12.5,
                450,
                450,
                240,
                35.2,
                12.1,
                Arrays.asList("forest", "alpine_meadow", "exposed_ridge"),
                Difficulty.MEDIUM,
                Arrays.asList("exposure", "weather_dependent"),
                "openstreetmap"
        );
        sampleTrail.setId(trailId);
    }

    @Test
    public void testGetAllTrails() throws Exception {
        when(trailApplicationService.getAllTrails())
                .thenReturn(Arrays.asList(sampleTrail));

        mockMvc.perform(get("/api/v1/trails")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Omu Peak Loop"))
                .andExpect(jsonPath("$[0].difficulty").value("MEDIUM"));

        verify(trailApplicationService, times(1)).getAllTrails();
    }

    @Test
    public void testGetTrailById() throws Exception {
        when(trailApplicationService.getTrail(trailId))
                .thenReturn(Optional.of(sampleTrail));

        mockMvc.perform(get("/api/v1/trails/{id}", trailId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Omu Peak Loop"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.distance").value(12.5));

        verify(trailApplicationService, times(1)).getTrail(trailId);
    }

    @Test
    public void testGetTrailByIdNotFound() throws Exception {
        when(trailApplicationService.getTrail(trailId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/trails/{id}", trailId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(trailApplicationService, times(1)).getTrail(trailId);
    }

    @Test
    public void testGetTrailsByDifficulty() throws Exception {
        when(trailApplicationService.getTrailsByDifficulty("MEDIUM"))
                .thenReturn(Arrays.asList(sampleTrail));

        mockMvc.perform(get("/api/v1/trails")
                .param("difficulty", "MEDIUM")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].difficulty").value("MEDIUM"));

        verify(trailApplicationService, times(1)).getTrailsByDifficulty("MEDIUM");
    }

    @Test
    public void testCreateTrail() throws Exception {
        Trail newTrail = new Trail(
                "Sphinx Ridge Scramble",
                "Technical scramble with rock climbing sections",
                8.3,
                680,
                680,
                320,
                65.0,
                35.5,
                Arrays.asList("scramble", "exposed_ridge", "rock"),
                Difficulty.ROCK_CLIMBING,
                Arrays.asList("exposure", "loose_rock", "high_altitude"),
                "openstreetmap"
        );
        newTrail.setId(UUID.randomUUID());

        when(trailApplicationService.createTrail(any(Trail.class)))
                .thenReturn(newTrail);

        mockMvc.perform(post("/api/v1/trails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTrail)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sphinx Ridge Scramble"))
                .andExpect(jsonPath("$.difficulty").value("ROCK_CLIMBING"));

        verify(trailApplicationService, times(1)).createTrail(any(Trail.class));
    }

    @Test
    public void testUpdateTrail() throws Exception {
        sampleTrail.setName("Updated Trail Name");

        when(trailApplicationService.getTrail(trailId))
                .thenReturn(Optional.of(sampleTrail));
        when(trailApplicationService.createTrail(any(Trail.class)))
                .thenReturn(sampleTrail);

        mockMvc.perform(put("/api/v1/trails/{id}", trailId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleTrail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Trail Name"));

        verify(trailApplicationService, times(1)).getTrail(trailId);
        verify(trailApplicationService, times(1)).createTrail(any(Trail.class));
    }

    @Test
    public void testDeleteTrail() throws Exception {
        doNothing().when(trailApplicationService).deleteTrail(trailId);

        mockMvc.perform(delete("/api/v1/trails/{id}", trailId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(trailApplicationService, times(1)).deleteTrail(trailId);
    }

    @Test
    public void testSuggestTrailsInArea() throws Exception {
        when(trailApplicationService.suggestTrailsInArea(45.5, 25.3, 10.0, "EASY"))
                .thenReturn(Arrays.asList(sampleTrail));

        mockMvc.perform(post("/api/v1/trails/suggest")
                .param("centerLat", "45.5")
                .param("centerLon", "25.3")
                .param("radiusKm", "10")
                .param("difficulty", "EASY")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(trailApplicationService, times(1)).suggestTrailsInArea(45.5, 25.3, 10.0, "EASY");
    }

    @Test
    public void testCreateTrailWithAutomaticDifficultyClassification() throws Exception {
        Trail trailWithoutDifficulty = new Trail(
                "Bulea Lake Forest Walk",
                "Easy forested walk with lake views",
                6.8,
                150,
                150,
                120,
                12.0,
                4.5,
                Arrays.asList("forest", "lake"),
                null,
                Arrays.asList(),
                "openstreetmap"
        );
        trailWithoutDifficulty.setId(UUID.randomUUID());

        Trail savedTrail = new Trail(
                "Bulea Lake Forest Walk",
                "Easy forested walk with lake views",
                6.8,
                150,
                150,
                120,
                12.0,
                4.5,
                Arrays.asList("forest", "lake"),
                Difficulty.EASY,
                Arrays.asList(),
                "openstreetmap"
        );
        savedTrail.setId(trailWithoutDifficulty.getId());

        when(trailApplicationService.createTrail(any(Trail.class)))
                .thenReturn(savedTrail);

        mockMvc.perform(post("/api/v1/trails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trailWithoutDifficulty)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.difficulty").value("EASY"));

        verify(trailApplicationService, times(1)).createTrail(any(Trail.class));
    }
}
