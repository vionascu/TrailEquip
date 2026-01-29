package com.trailequip.trail.domain.repository;

import com.trailequip.trail.domain.model.Trail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrailRepository extends JpaRepository<Trail, UUID> {
    List<Trail> findByDifficulty(String difficulty);

    // For now, return trails by difficulty (geographic queries require PostGIS)
    @Query("SELECT t FROM Trail t WHERE t.difficulty = :difficulty OR :difficulty IS NULL ORDER BY t.name")
    List<Trail> findTrailsInArea(@Param("difficulty") String difficulty);
}
