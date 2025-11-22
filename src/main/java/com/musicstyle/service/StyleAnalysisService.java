package com.musicstyle.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StyleAnalysisService {

    /**
     * WebClient instance for making HTTP requests to the Spotify API.
     * This client is used to interact with Spotify's web services for music analysis and data retrieval.
     */
    private final WebClient spotifyWebClient;

    public Map<String, Object> generateStyleProfile(String accessToken) {
        Map response = spotifyWebClient.get()
                .uri("/me/top/artists")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Alimentar com mais conhecimentos pessoais e/ou analise de uma LLM (possibilidade  futura) 
        return Map.of(
                "style", "minimalista urbano",
                "genresAnalyzed", response.get("items")
        );
    }
}
