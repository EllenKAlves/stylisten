package com.stylisten.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylisten.api.dto.*;
import com.stylisten.domain.entity.*;
import com.stylisten.domain.repository.*;
import com.stylisten.infrastructure.exception.ResourceNotFoundException;
import com.stylisten.infrastructure.spotify.SpotifyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final SpotifyAccountRepository spotifyAccountRepository;
    private final TrackCacheRepository trackCacheRepository;
    private final GenreStatRepository genreStatRepository;
    private final GenreStyleMappingRepository mappingRepository;
    private final StyleRepository styleRepository;
    private final SpotifyClient spotifyClient;
    private final ObjectMapper objectMapper;

    @Value("${stylisten.profile.history-days:30}")
    private Integer historyDays;

    @Value("${stylisten.profile.top-genres-limit:5}")
    private Integer topGenresLimit;

    @Value("${spotify.cache.ttl-hours:6}")
    private Integer cacheTtlHours;

    @Transactional
    public ProfileResponse generateProfile(UUID userId, boolean forceRefresh) {
        log.info("Gerando perfil para usuário: {}", userId);

        SpotifyAccount account = spotifyAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conta Spotify não vinculada"));

        //booleano pra atualizar cache
        boolean needsRefresh = forceRefresh || 
            shouldRefreshCache(account.getLastSyncAt());

        if (needsRefresh) {
            syncTracksFromSpotify(account);
        }

        //estatistica de genero (musical)
        LocalDate periodStart = LocalDate.now().minusDays(historyDays);
        LocalDate periodEnd = LocalDate.now();
        
        Map<String, Integer> genreCounts = calculateGenreCounts(
            userId, 
            periodStart.atStartOfDay(ZoneOffset.UTC).toInstant()
        );

        List<GenreStat> genreStats = normalizeAndSaveGenreStats(
            userId, 
            genreCounts, 
            periodStart, 
            periodEnd
        );

        List<GenreStat> topGenres = genreStats.stream()
            .sorted(Comparator.comparing(GenreStat::getNormalizedScore).reversed())
            .limit(topGenresLimit)
            .toList();

        //match de estilos 
        List<MatchingStyle> matchingStyles = findMatchingStyles(topGenres);

        return ProfileResponse.builder()
            .userId(userId)
            .generatedAt(Instant.now())
            .topGenres(topGenres.stream()
                .map(g -> GenreScore.builder()
                    .genre(g.getGenreName())
                    .score(g.getNormalizedScore())
                    .build())
                .toList())
            .matchingStyles(matchingStyles)
            .build();
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        LocalDate periodStart = LocalDate.now().minusDays(historyDays);
        
        List<GenreStat> stats = genreStatRepository
            .findTopGenresByUserAndPeriod(userId, periodStart);

        if (stats.isEmpty()) {
            throw new ResourceNotFoundException("Perfil não encontrado. Execute /generate primeiro.");
        }

        List<GenreStat> topGenres = stats.stream()
            .limit(topGenresLimit)
            .toList();

        List<MatchingStyle> matchingStyles = findMatchingStyles(topGenres);

        return ProfileResponse.builder()
            .userId(userId)
            .generatedAt(Instant.now())
            .topGenres(topGenres.stream()
                .map(g -> GenreScore.builder()
                    .genre(g.getGenreName())
                    .score(g.getNormalizedScore())
                    .build())
                .toList())
            .matchingStyles(matchingStyles)
            .build();
    }

    private boolean shouldRefreshCache(Instant lastSync) {
        if (lastSync == null) return true;
        
        Instant threshold = Instant.now().minus(Duration.ofHours(cacheTtlHours));
        return lastSync.isBefore(threshold);
    }

    private void syncTracksFromSpotify(SpotifyAccount account) {
        log.info("Sincronizando tracks do Spotify para usuário: {}", account.getUser().getId());

        Instant after = Instant.now().minus(Duration.ofDays(historyDays));
        
        List<SpotifyPlayHistoryItem> items = spotifyClient.getRecentlyPlayed(
            account.getAccessToken(), 
            after, 
            50
        );

        List<TrackCache> trackCaches = items.stream()
            .map(item -> convertToTrackCache(item, account))
            .toList();

        trackCacheRepository.saveAll(trackCaches);
        
        account.setLastSyncAt(Instant.now());
        spotifyAccountRepository.save(account);

        log.info("Sincronizados {} tracks", trackCaches.size());
    }

    private TrackCache convertToTrackCache(
        SpotifyPlayHistoryItem item, 
        SpotifyAccount account
    ) {
        List<String> genres = new ArrayList<>();
        
        //cha revelação de generos dos artistas
        for (SpotifyArtist artist : item.getTrack().getArtists()) {
            if (artist.getGenres() != null && !artist.getGenres().isEmpty()) {
                genres.addAll(artist.getGenres());
            } else {
                SpotifyArtist fullArtist = spotifyClient.getArtist(
                    account.getAccessToken(), 
                    artist.getId()
                );
                if (fullArtist != null && fullArtist.getGenres() != null) {
                    genres.addAll(fullArtist.getGenres());
                }
            }
        }

        try {
            return TrackCache.builder()
                .spotifyTrackId(item.getTrack().getId())
                .user(account.getUser())
                .playedAt(Instant.parse(item.getPlayedAt()))
                .artistName(item.getTrack().getArtists().get(0).getName())
                .trackName(item.getTrack().getName())
                .genres(objectMapper.writeValueAsString(genres))
                .build();
        } catch (Exception e) {
            log.error("Erro ao converter track: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Map<String, Integer> calculateGenreCounts(UUID userId, Instant since) {
        List<TrackCache> tracks = trackCacheRepository
            .findByUserIdAndPlayedAtBetween(userId, since, Instant.now());

        Map<String, Integer> counts = new HashMap<>();

        for (TrackCache track : tracks) {
            try {
                List<String> genres = objectMapper.readValue(
                    track.getGenres(), 
                    new TypeReference<List<String>>() {}
                );
                
                for (String genre : genres) {
                    counts.merge(genre.toLowerCase(), 1, Integer::sum);
                }
            } catch (Exception e) {
                log.warn("Erro ao processar gêneros do track {}: {}", 
                    track.getId(), e.getMessage());
            }
        }

        return counts;
    }

    private List<GenreStat> normalizeAndSaveGenreStats(
        UUID userId,
        Map<String, Integer> genreCounts,
        LocalDate periodStart,
        LocalDate periodEnd
    ) {
        if (genreCounts.isEmpty()) {
            return Collections.emptyList();
        }

        int maxCount = genreCounts.values().stream()
            .max(Integer::compareTo)
            .orElse(1);

        List<GenreStat> stats = new ArrayList<>();
        User user = new User();
        user.setId(userId);

        for (Map.Entry<String, Integer> entry : genreCounts.entrySet()) {
            double normalizedScore = 10.0 * entry.getValue() / maxCount;
            
            GenreStat stat = GenreStat.builder()
                .user(user)
                .genreName(entry.getKey())
                .rawCount(entry.getValue())
                .normalizedScore(normalizedScore)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
            
            stats.add(stat);
        }

        return genreStatRepository.saveAll(stats);
    }

    private List<MatchingStyle> findMatchingStyles(List<GenreStat> topGenres) {
        List<String> genreNames = topGenres.stream()
            .map(GenreStat::getGenreName)
            .toList();

        List<GenreStyleMapping> mappings = mappingRepository.findByGenreNameIn(genreNames);

        Map<UUID, Double> styleScores = new HashMap<>();
        Map<UUID, Style> styleMap = new HashMap<>();

        for (GenreStyleMapping mapping : mappings) {
            UUID styleId = mapping.getStyle().getId();
            styleMap.put(styleId, mapping.getStyle());
            
            double score = styleScores.getOrDefault(styleId, 0.0);
            score += mapping.getWeight();
            styleScores.put(styleId, score);
        }

        double maxScore = styleScores.values().stream()
            .max(Double::compareTo)
            .orElse(1.0);

        return styleScores.entrySet().stream()
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Style style = styleMap.get(entry.getKey());
                return MatchingStyle.builder()
                    .styleId(style.getId())
                    .name(style.getName())
                    .confidence(entry.getValue() / maxScore)
                    .description(style.getDescription())
                    .tags(style.getTags() != null ? 
                        Arrays.asList(style.getTags()) : Collections.emptyList())
                    .build();
            })
            .toList();
    }
}