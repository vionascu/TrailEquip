package com.trailequip.recommendation.adapter.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trailequip.recommendation.application.service.EquipmentRecommendationService;
import com.trailequip.recommendation.application.service.TrailRecommendationService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentRecommendationService equipmentRecommendationService;

    @MockBean
    private TrailRecommendationService trailRecommendationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> recommendationRequest;
    private Map<String, Object> equipmentRecommendation;
    private List<Map<String, Object>> trailRecommendations;

    @BeforeEach
    public void setup() {
        recommendationRequest = new HashMap<>();
        recommendationRequest.put("trailDifficulty", "MEDIUM");
        recommendationRequest.put("temperature", 15);
        recommendationRequest.put("rainProbability", 30);
        recommendationRequest.put("windSpeed", 20);

        equipmentRecommendation = new HashMap<>();
        equipmentRecommendation.put("trailName", "Omu Peak Loop");
        equipmentRecommendation.put("difficulty", "MEDIUM");
        equipmentRecommendation.put(
                "recommendedGear",
                Arrays.asList("Hiking Boots", "Rain Jacket", "Backpack (30L)", "Water Bottle", "Sun Protection"));
        equipmentRecommendation.put(
                "warnings", Arrays.asList("Exposure on ridge", "Weather-dependent", "Bring extra layers"));
        equipmentRecommendation.put(
                "explanation", "MEDIUM difficulty trail with exposure and changeable weather requires proper gear");

        Map<String, Object> trail1 = new HashMap<>();
        trail1.put("id", "trail-1");
        trail1.put("name", "Bulea Lake Forest Walk");
        trail1.put("difficulty", "EASY");
        trail1.put("distance", 6.8);
        trail1.put("score", 0.95);
        trail1.put("reason", "Perfect for current weather conditions");

        Map<String, Object> trail2 = new HashMap<>();
        trail2.put("id", "trail-2");
        trail2.put("name", "Sphinx Ridge Scramble");
        trail2.put("difficulty", "ROCK_CLIMBING");
        trail2.put("distance", 8.3);
        trail2.put("score", 0.65);
        trail2.put("reason", "Challenging but doable with proper preparation");

        trailRecommendations = Arrays.asList(trail1, trail2);
    }

    @Test
    public void testGetEquipmentRecommendations() throws Exception {
        when(equipmentRecommendationService.recommendEquipment(any())).thenReturn(equipmentRecommendation);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trailName").value("Omu Peak Loop"))
                .andExpect(jsonPath("$.recommendedGear", hasSize(5)))
                .andExpect(jsonPath("$.recommendedGear[0]").value("Hiking Boots"))
                .andExpect(jsonPath("$.warnings", hasSize(3)));

        verify(equipmentRecommendationService, times(1)).recommendEquipment(any());
    }

    @Test
    public void testGetTrailRecommendations() throws Exception {
        when(trailRecommendationService.recommendTrails(any())).thenReturn(trailRecommendations);

        mockMvc.perform(post("/api/v1/recommendations/trails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Bulea Lake Forest Walk"))
                .andExpect(jsonPath("$[0].score").value(0.95))
                .andExpect(jsonPath("$[1].difficulty").value("ROCK_CLIMBING"));

        verify(trailRecommendationService, times(1)).recommendTrails(any());
    }

    @Test
    public void testGetEquipmentRecommendationsForEasyTrail() throws Exception {
        Map<String, Object> easyRecommendation = new HashMap<>(equipmentRecommendation);
        easyRecommendation.put("recommendedGear", Arrays.asList("Casual Hiking Shoes", "Light Jacket", "Water Bottle"));

        when(equipmentRecommendationService.recommendEquipment(any())).thenReturn(easyRecommendation);

        Map<String, Object> easyRequest = new HashMap<>(recommendationRequest);
        easyRequest.put("trailDifficulty", "EASY");

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(easyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedGear", hasSize(3)));

        verify(equipmentRecommendationService, times(1)).recommendEquipment(any());
    }

    @Test
    public void testGetEquipmentRecommendationsForExtremeWeather() throws Exception {
        Map<String, Object> extremeWeatherRecommendation = new HashMap<>(equipmentRecommendation);
        extremeWeatherRecommendation.put(
                "warnings",
                Arrays.asList(
                        "EXTREME: High wind speed",
                        "Risk of hypothermia",
                        "Trail may be impassable",
                        "Consider turning back"));

        when(equipmentRecommendationService.recommendEquipment(any())).thenReturn(extremeWeatherRecommendation);

        Map<String, Object> extremeRequest = new HashMap<>(recommendationRequest);
        extremeRequest.put("windSpeed", 50);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(extremeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warnings", hasSize(4)));

        verify(equipmentRecommendationService, times(1)).recommendEquipment(any());
    }

    @Test
    public void testGetTrailRecommendationsSortedByScore() throws Exception {
        when(trailRecommendationService.recommendTrails(any())).thenReturn(trailRecommendations);

        mockMvc.perform(post("/api/v1/recommendations/trails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(0.95))
                .andExpect(jsonPath("$[1].score").value(0.65));

        verify(trailRecommendationService, times(1)).recommendTrails(any());
    }

    @Test
    public void testRecommendationRequestValidation() throws Exception {
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("trailDifficulty", "INVALID_DIFFICULTY");

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetBestTrailMatch() throws Exception {
        Map<String, Object> bestMatch = trailRecommendations.get(0);

        when(trailRecommendationService.getBestTrailMatch(any())).thenReturn(bestMatch);

        mockMvc.perform(post("/api/v1/recommendations/trails/best")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bulea Lake Forest Walk"))
                .andExpect(jsonPath("$.score").value(0.95));

        verify(trailRecommendationService, times(1)).getBestTrailMatch(any());
    }

    @Test
    public void testGetRiskAssessment() throws Exception {
        Map<String, Object> riskAssessment = new HashMap<>();
        riskAssessment.put("overallRisk", "MODERATE");
        riskAssessment.put("weatherRisk", "LOW");
        riskAssessment.put("terrainRisk", "MODERATE");
        riskAssessment.put(
                "recommendations",
                Arrays.asList(
                        "Start early to avoid evening storms",
                        "Bring emergency shelter",
                        "Let someone know your plans"));

        when(equipmentRecommendationService.getTrailRiskAssessment(any())).thenReturn(riskAssessment);

        mockMvc.perform(post("/api/v1/recommendations/risk-assessment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallRisk").value("MODERATE"))
                .andExpect(jsonPath("$.recommendations", hasSize(3)));

        verify(equipmentRecommendationService, times(1)).getTrailRiskAssessment(any());
    }
}
