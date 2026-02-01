package com.trailequip.weather.adapter.rest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trailequip.weather.application.service.WeatherApplicationService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherApplicationService weatherApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> sampleWeather;

    @BeforeEach
    public void setup() {
        sampleWeather = new HashMap<>();
        sampleWeather.put("temperature", 15.0);
        sampleWeather.put("wind_speed", 10.0);
        sampleWeather.put("rain_probability", 20.0);
        sampleWeather.put("condition", "Partly Cloudy");
        sampleWeather.put("location", "Omu Peak");
        sampleWeather.put("timestamp", System.currentTimeMillis());
    }

    @Test
    public void testGetWeatherForecast() throws Exception {
        when(weatherApplicationService.getWeatherForecast(
                        45.5, 25.3, LocalDate.now(), LocalDate.now().plusDays(7)))
                .thenReturn(sampleWeather);

        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "45.5")
                        .param("longitude", "25.3")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(7).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(15.0))
                .andExpect(jsonPath("$.wind_speed").value(10.0))
                .andExpect(jsonPath("$.rain_probability").value(20.0));

        verify(weatherApplicationService, times(1))
                .getWeatherForecast(45.5, 25.3, LocalDate.now(), LocalDate.now().plusDays(7));
    }

    @Test
    public void testGetWeatherForecastWithDefaults() throws Exception {
        when(weatherApplicationService.getWeatherForecast(45.5, 25.3)).thenReturn(sampleWeather);

        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "45.5")
                        .param("longitude", "25.3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(15.0));

        verify(weatherApplicationService, times(1)).getWeatherForecast(45.5, 25.3);
    }

    @Test
    public void testGetWeatherCacheStatistics() throws Exception {
        Map<String, Object> cacheStats = new HashMap<>();
        cacheStats.put("total_cached_forecasts", 42);
        cacheStats.put("cache_hit_rate", 0.85);
        cacheStats.put("average_cache_age_hours", 3.5);

        when(weatherApplicationService.getCacheStatistics()).thenReturn(cacheStats);

        mockMvc.perform(get("/api/v1/weather/cache/stats").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_cached_forecasts").value(42))
                .andExpect(jsonPath("$.cache_hit_rate").value(0.85));

        verify(weatherApplicationService, times(1)).getCacheStatistics();
    }

    @Test
    public void testClearWeatherCache() throws Exception {
        doNothing().when(weatherApplicationService).clearCache();

        mockMvc.perform(delete("/api/v1/weather/cache").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(weatherApplicationService, times(1)).clearCache();
    }

    @Test
    public void testGetWeatherForMultipleLocations() throws Exception {
        Map<String, Map<String, Object>> multiLocationWeather = new HashMap<>();
        multiLocationWeather.put("location_1", sampleWeather);

        Map<String, Object> weather2 = new HashMap<>();
        weather2.put("temperature", 12.0);
        weather2.put("wind_speed", 15.0);
        weather2.put("rain_probability", 40.0);
        multiLocationWeather.put("location_2", weather2);

        when(weatherApplicationService.getWeatherForMultipleLocations(any(), any()))
                .thenReturn(multiLocationWeather);

        mockMvc.perform(post("/api/v1/weather/multi-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new double[][] {{45.5, 25.3}, {44.5, 24.3}})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location_1.temperature").value(15.0))
                .andExpect(jsonPath("$.location_2.temperature").value(12.0));

        verify(weatherApplicationService, times(1)).getWeatherForMultipleLocations(any(), any());
    }

    @Test
    public void testWeatherForecastInvalidCoordinates() throws Exception {
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "999")
                        .param("longitude", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
