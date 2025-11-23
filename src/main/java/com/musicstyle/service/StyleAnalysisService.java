package com.stylist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyMusicAnalysisService {

    private final WebClient spotifyWebClient;

    /**
     * Busca as top tracks do usuário no último mês
     * Endpoint: GET /me/top/tracks?time_range=short_term&limit=50
     */
    public List<Map<String, Object>> fetchRecentTopTracks(String accessToken) {
        Map<String, Object> response = spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/top/tracks")
                        .queryParam("time_range", "short_term") // último mês
                        .queryParam("limit", 50)
                        .build())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (List<Map<String, Object>>) response.get("items");
    }

    /**
     * Extrai gêneros dos artistas das tracks
     * Faz chamadas adicionais para GET /artists/{id} para cada artista
     */
    public Map<String, Integer> extractGenreFrequency(
            List<Map<String, Object>> tracks, 
            String accessToken) {
        
        Map<String, Integer> genreFrequency = new HashMap<>();
        Set<String> processedArtistIds = new HashSet<>();

        for (Map<String, Object> track : tracks) {
            List<Map<String, Object>> artists = 
                (List<Map<String, Object>>) track.get("artists");

            for (Map<String, Object> artist : artists) {
                String artistId = (String) artist.get("id");
                
                // Evitar chamadas duplicadas
                if (processedArtistIds.contains(artistId)) {
                    continue;
                }
                processedArtistIds.add(artistId);

                // Buscar detalhes do artista para obter gêneros
                List<String> genres = fetchArtistGenres(artistId, accessToken);
                
                // Incrementar contagem de cada gênero
                genres.forEach(genre -> 
                    genreFrequency.merge(
                        genre.toLowerCase(), 
                        1, 
                        Integer::sum
                    )
                );
            }
        }

        log.info("Gêneros encontrados: {}", genreFrequency);
        return genreFrequency;
    }

    /**
     * Busca gêneros de um artista específico
     */
    private List<String> fetchArtistGenres(String artistId, String accessToken) {
        try {
            Map<String, Object> artistData = spotifyWebClient.get()
                    .uri("/artists/" + artistId)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return (List<String>) artistData.getOrDefault("genres", List.of());
        } catch (Exception e) {
            log.error("Erro ao buscar gêneros do artista {}: {}", 
                      artistId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Normaliza gêneros similares
     * Ex: "hip hop", "hip-hop", "hiphop" -> "hip hop"
     */
    public Map<String, Integer> normalizeGenres(Map<String, Integer> genreFrequency) {
        Map<String, Integer> normalized = new HashMap<>();
        
        genreFrequency.forEach((genre, count) -> {
            String normalizedGenre = genre
                .toLowerCase()
                .replaceAll("[\\s-_]+", " ")
                .trim();
            
            normalized.merge(normalizedGenre, count, Integer::sum);
        });
        
        return normalized;
    }
}