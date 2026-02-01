package com.trailequip.recommendation.adapter.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trailequip.recommendation.application.service.EquipmentRecommendationService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentRecommendationService equipmentRecommendationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, String> recommendationRequest;
    private Map<String, Object> equipmentRecommendation;
    private UUID trailId;

    @BeforeEach
    public void setup() {
        trailId = UUID.randomUUID();
        recommendationRequest = new HashMap<>();
        recommendationRequest.put("trailId", trailId.toString());
        recommendationRequest.put("forecastStart", "2024-01-30");
        recommendationRequest.put("forecastEnd", "2024-02-05");

        Map<String, Object> equipment = new HashMap<>();
        equipment.put("category", "LAYERS");
        equipment.put("items", Arrays.asList("Base Layer (Thermal)", "Mid Layer (Fleece)", "Outer Layer (Waterproof)"));

        equipmentRecommendation = new HashMap<>();
        equipmentRecommendation.put("equipment", Arrays.asList(equipment));
        equipmentRecommendation.put("warnings", Arrays.asList("High wind expected on ridges"));
        equipmentRecommendation.put("summary", "Moderate conditions; bring layered system and rain protection");
    }

    @Test
    public void testGetEquipmentRecommendations() throws Exception {
        when(equipmentRecommendationService.recommend(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(equipmentRecommendation);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipment").isArray())
                .andExpect(jsonPath("$.equipment[0].category").value("LAYERS"))
                .andExpect(jsonPath("$.warnings").isArray())
                .andExpect(jsonPath("$.warnings[0]").value("High wind expected on ridges"))
                .andExpect(
                        jsonPath("$.summary").value("Moderate conditions; bring layered system and rain protection"));

        verify(equipmentRecommendationService, times(1))
                .recommend(any(UUID.class), any(String.class), any(String.class));
    }

    @Test
    public void testGetEquipmentRecommendationsWithDifferentForecast() throws Exception {
        Map<String, Object> differentRecommendation = new HashMap<>(equipmentRecommendation);
        differentRecommendation.put("warnings", Arrays.asList("Extreme cold", "Heavy snow expected"));

        when(equipmentRecommendationService.recommend(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(differentRecommendation);

        Map<String, String> winterRequest = new HashMap<>(recommendationRequest);
        winterRequest.put("forecastStart", "2024-02-01");
        winterRequest.put("forecastEnd", "2024-02-10");

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(winterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warnings").isArray())
                .andExpect(jsonPath("$.warnings[0]").value("Extreme cold"));

        verify(equipmentRecommendationService, times(1))
                .recommend(any(UUID.class), any(String.class), any(String.class));
    }

    @Test
    public void testGetEquipmentRecommendationsSuccess() throws Exception {
        when(equipmentRecommendationService.recommend(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(equipmentRecommendation);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk());

        verify(equipmentRecommendationService, times(1))
                .recommend(any(UUID.class), any(String.class), any(String.class));
    }

    @Test
    public void testRecommendationRequestWithValidUUID() throws Exception {
        when(equipmentRecommendationService.recommend(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(equipmentRecommendation);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipment").exists());

        verify(equipmentRecommendationService, times(1))
                .recommend(any(UUID.class), any(String.class), any(String.class));
    }

    @Test
    public void testRecommendationReturnsWarnings() throws Exception {
        when(equipmentRecommendationService.recommend(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(equipmentRecommendation);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warnings").isArray())
                .andExpect(jsonPath("$.warnings.length()").value(1));

        verify(equipmentRecommendationService, times(1))
                .recommend(any(UUID.class), any(String.class), any(String.class));
    }

    @Test
    public void testRecommendationReturnsSummary() throws Exception {
        when(equipmentRecommendationService.recommend(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(equipmentRecommendation);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists());

        verify(equipmentRecommendationService, times(1))
                .recommend(any(UUID.class), any(String.class), any(String.class));
    }

    @Test
    public void testRecommendationWithMultipleEquipmentCategories() throws Exception {
        Map<String, Object> layersEquipment = new HashMap<>();
        layersEquipment.put("category", "LAYERS");
        layersEquipment.put("items", Arrays.asList("Base Layer", "Mid Layer", "Outer Layer"));

        Map<String, Object> outerEquipment = new HashMap<>();
        outerEquipment.put("category", "OUTERWEAR");
        outerEquipment.put("items", Arrays.asList("Rain Jacket", "Wind Pants"));

        Map<String, Object> multipleEquipmentResponse = new HashMap<>();
        multipleEquipmentResponse.put("equipment", Arrays.asList(layersEquipment, outerEquipment));
        multipleEquipmentResponse.put("warnings", Arrays.asList("High wind expected"));
        multipleEquipmentResponse.put("summary", "Complex conditions ahead");

        when(equipmentRecommendationService.recommend(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(multipleEquipmentResponse);

        mockMvc.perform(post("/api/v1/recommendations/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipment.length()").value(2));

        verify(equipmentRecommendationService, times(1))
                .recommend(any(UUID.class), any(String.class), any(String.class));
    }
}
