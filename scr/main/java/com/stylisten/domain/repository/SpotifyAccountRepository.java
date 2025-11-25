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
public interface SpotifyAccountRepository extends JpaRepository<SpotifyAccount, UUID> {
    Optional<SpotifyAccount> findByUserId(UUID userId);
    Optional<SpotifyAccount> findBySpotifyUserId(String spotifyUserId);
    boolean existsByUserId(UUID userId);
}
