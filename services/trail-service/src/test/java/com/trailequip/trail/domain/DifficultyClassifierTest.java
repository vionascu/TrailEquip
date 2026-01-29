package com.trailequip.trail.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import com.trailequip.trail.domain.service.DifficultyClassifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DifficultyClassifierTest {

    private DifficultyClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new DifficultyClassifier();
    }

    @Test
    void testClassifyEasyTrail() {
        Trail trail = new Trail(
                "Easy Walk",
                "Simple forest walk",
                6.8,
                150,
                150,
                120,
                12.0,
                4.5,
                List.of("forest", "lake"),
                null,
                List.of(),
                "openstreetmap");

        Difficulty result = classifier.classify(trail);
        assertEquals(Difficulty.EASY, result);
    }

    @Test
    void testClassifyMediumTrail() {
        Trail trail = new Trail(
                "Omu Peak Loop",
                "Alpine route",
                12.5,
                450,
                450,
                240,
                35.2,
                12.1,
                List.of("forest", "alpine_meadow", "exposed_ridge"),
                null,
                List.of("exposure"),
                "openstreetmap");

        Difficulty result = classifier.classify(trail);
        assertEquals(Difficulty.MEDIUM, result);
    }

    @Test
    void testClassifyRockClimbingTrail() {
        Trail trail = new Trail(
                "Sphinx Ridge Scramble",
                "Technical scramble",
                8.3,
                680,
                680,
                320,
                65.0,
                35.5,
                List.of("scramble", "exposed_ridge", "rock"),
                null,
                List.of("exposure"),
                "openstreetmap");

        Difficulty result = classifier.classify(trail);
        assertEquals(Difficulty.ROCK_CLIMBING, result);
    }
}
