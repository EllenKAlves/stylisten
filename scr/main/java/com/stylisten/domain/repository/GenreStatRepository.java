package com.stylisten.domain.repository;

import com.stylisten.domain.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreStatRepository extends JpaRepository<GenreStat, UUID> {
    List<GenreStat> findByUserIdAndPeriodStartOrderByNormalizedScoreDesc(
        UUID userId, 
        LocalDate periodStart
    );

    @Query("SELECT g FROM GenreStat g WHERE g.user.id = :userId " +
           "AND g.periodStart = :periodStart " +
           "ORDER BY g.normalizedScore DESC")
    List<GenreStat> findTopGenresByUserAndPeriod(
        @Param("userId") UUID userId, 
        @Param("periodStart") LocalDate periodStart
    );

    void deleteByUserIdAndPeriodStartBefore(UUID userId, LocalDate before);
}