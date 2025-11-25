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
public interface TrackCacheRepository extends JpaRepository<TrackCache, UUID> {
    List<TrackCache> findByUserIdAndPlayedAtBetween(
        UUID userId, 
        Instant startDate, 
        Instant endDate
    );

    @Query("SELECT COUNT(t) FROM TrackCache t WHERE t.user.id = :userId " +
           "AND t.playedAt >= :since")
    long countByUserIdAndPlayedAtAfter(
        @Param("userId") UUID userId, 
        @Param("since") Instant since
    );

    void deleteByUserIdAndPlayedAtBefore(UUID userId, Instant before);
}