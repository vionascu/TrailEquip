package com.trailequip.trail.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Difficulty enum.
 * Tests difficulty inference from metrics and validation logic.
 */
class DifficultyTest {

    @Test
    void shouldInferEasyFromLowMetrics() {
        Difficulty difficulty = Difficulty.inferFromMetrics(300, 8.0);
        assertEquals(Difficulty.EASY, difficulty);
    }

    @Test
    void shouldInferMediumFromModerateMetrics() {
        Difficulty difficulty = Difficulty.inferFromMetrics(1000, 15.0);
        assertEquals(Difficulty.MEDIUM, difficulty);
    }

    @Test
    void shouldInferHardFromHighMetrics() {
        Difficulty difficulty = Difficulty.inferFromMetrics(2000, 28.0);
        assertEquals(Difficulty.HARD, difficulty);
    }

    @Test
    void shouldInferAlpineFromAlpineMetrics() {
        Difficulty difficulty = Difficulty.inferFromMetrics(2800, 38.0);
        assertEquals(Difficulty.ALPINE, difficulty);
    }

    @Test
    void shouldInferScramblingFromScramblingMetrics() {
        Difficulty difficulty = Difficulty.inferFromMetrics(3200, 55.0);
        assertEquals(Difficulty.SCRAMBLING, difficulty);
    }

    @Test
    void shouldDefaultToMediumWhenMetricsNull() {
        Difficulty difficulty = Difficulty.inferFromMetrics(null, null);
        assertEquals(Difficulty.MEDIUM, difficulty);
    }

    @Test
    void shouldPrioritizeSlopeOverElevation() {
        // High slope should result in higher difficulty even with low elevation
        Difficulty difficulty = Difficulty.inferFromMetrics(500, 45.0);
        assertEquals(Difficulty.ALPINE, difficulty);
    }

    @ParameterizedTest
    @CsvSource({
        "400, 9.0, true",     // EASY matches
        "600, 12.0, false",    // EASY doesn't match (elevation too high)
        "1200, 18.0, true",    // MEDIUM matches
        "1600, 25.0, true",    // HARD matches
    })
    void shouldValidateMetricsAgainstDifficulty(int elevation, double slope, boolean shouldMatch) {
        boolean result = Difficulty.EASY.matches(elevation, slope);
        assertEquals(shouldMatch, result);
    }

    @Test
    void shouldHaveCorrectEmoji() {
        assertEquals("🟢", Difficulty.EASY.getEmoji());
        assertEquals("🟡", Difficulty.MEDIUM.getEmoji());
        assertEquals("🔴", Difficulty.HARD.getEmoji());
        assertEquals("🟣", Difficulty.ALPINE.getEmoji());
        assertEquals("🧗", Difficulty.SCRAMBLING.getEmoji());
    }

    @Test
    void shouldHaveDescriptions() {
        assertNotNull(Difficulty.EASY.getDescription());
        assertFalse(Difficulty.EASY.getDescription().isEmpty());
        assertTrue(Difficulty.EASY.getDescription().contains("Easy"));
    }

    @Test
    void shouldHaveSlopeThresholds() {
        assertEquals(10.0, Difficulty.EASY.getMaxSlopeThreshold());
        assertEquals(20.0, Difficulty.MEDIUM.getMaxSlopeThreshold());
        assertEquals(30.0, Difficulty.HARD.getMaxSlopeThreshold());
        assertEquals(40.0, Difficulty.ALPINE.getMaxSlopeThreshold());
        assertEquals(50.0, Difficulty.SCRAMBLING.getMaxSlopeThreshold());
    }

    @Test
    void shouldHaveElevationThresholds() {
        assertEquals(500, Difficulty.EASY.getMaxElevationGainThreshold());
        assertEquals(1500, Difficulty.MEDIUM.getMaxElevationGainThreshold());
        assertEquals(2500, Difficulty.HARD.getMaxElevationGainThreshold());
        assertEquals(3000, Difficulty.ALPINE.getMaxElevationGainThreshold());
        assertEquals(3500, Difficulty.SCRAMBLING.getMaxElevationGainThreshold());
    }
}
