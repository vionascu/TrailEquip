package com.trailequip.trail.domain.repository;

import com.trailequip.trail.domain.model.Difficulty;
import com.trailequip.trail.domain.model.Trail;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrailRepository extends JpaRepository<Trail, UUID> {
    @Query("SELECT t FROM Trail t WHERE t.difficulty = :difficulty ORDER BY t.name")
    List<Trail> findByDifficulty(@Param("difficulty") Difficulty difficulty);

    // For now, return trails by difficulty (geographic queries require PostGIS)
    @Query("SELECT t FROM Trail t WHERE t.difficulty = :difficulty OR :difficulty IS NULL ORDER BY t.name")
    List<Trail> findTrailsInArea(@Param("difficulty") Difficulty difficulty);

    // OSM Integration Queries
    Optional<Trail> findByOsmId(Long osmId);

    List<Trail> findBySource(String source);

    @Query("SELECT t FROM Trail t WHERE t.osmId IN :osmIds")
    List<Trail> findByOsmIds(@Param("osmIds") List<Long> osmIds);

    @Query("SELECT t FROM Trail t WHERE t.difficulty = :difficulty ORDER BY t.distance DESC")
    List<Trail> findByDifficultyOrderByDistance(@Param("difficulty") Difficulty difficulty);

    @Query("SELECT t FROM Trail t WHERE t.source = :source ORDER BY t.createdAt DESC")
    List<Trail> findRecentTrailsBySource(@Param("source") String source, Pageable pageable);
}
