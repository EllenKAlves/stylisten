package com.stylisten.infrastructure.spotify;

import com.stylisten.api.dto.*;
import com.stylisten.infrastructure.exception.SpotifyApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SpotifyClient {

    private final WebClient webClient;
    private final String baseUrl;

    public SpotifyClient(
        WebClient.Builder webClientBuilder,
        @Value("${spotify.api.base-url}") String baseUrl
    ) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }

    public SpotifyUserProfile getUserProfile(String accessToken) {
        try {
            return webClient.get()
                .uri("/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifyUserProfile.class)
                .retryWhen(createRetrySpec())
                .block();
        } catch (WebClientResponseException e) {
            log.error("Erro ao buscar perfil do usuário: {}", e.getMessage());
            throw new SpotifyApiException("Falha ao buscar perfil do Spotify", e);
        }
    }

    public List<SpotifyPlayHistoryItem> getRecentlyPlayed(
        String accessToken, 
        Instant after, 
        Integer limit
    ) {
        List<SpotifyPlayHistoryItem> allItems = new ArrayList<>();
        String nextUrl = null;
        
        try {
            do {
                SpotifyRecentlyPlayedResponse response = fetchRecentlyPlayed(
                    accessToken, 
                    after, 
                    limit, 
                    nextUrl
                );
                
                if (response != null && response.getItems() != null) {
                    allItems.addAll(response.getItems());
                    nextUrl = response.getNextUrl();
                } else {
                    break;
                }
                
                //sem loopings infinitos
                if (allItems.size() >= 1000) {
                    log.warn("Limite de 1000 tracks alcançado, parando paginação");
                    break;
                }
                
            } while (nextUrl != null);
            
            return allItems;
            
        } catch (Exception e) {
            log.error("Erro ao buscar histórico de reprodução: {}", e.getMessage());
            throw new SpotifyApiException("Falha ao buscar histórico do Spotify", e);
        }
    }

    private SpotifyRecentlyPlayedResponse fetchRecentlyPlayed(
        String accessToken, 
        Instant after, 
        Integer limit,
        String nextUrl
    ) {
        if (nextUrl != null && !nextUrl.isEmpty()) {
            return webClient.get()
                .uri(nextUrl.replace(baseUrl, ""))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifyRecentlyPlayedResponse.class)
                .retryWhen(createRetrySpec())
                .block();
        }
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/me/player/recently-played")
                .queryParam("limit", limit != null ? limit : 50)
                .queryParamIfPresent("after", 
                    after != null ? 
                    java.util.Optional.of(after.toEpochMilli()) : 
                    java.util.Optional.empty()
                )
                .build())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(SpotifyRecentlyPlayedResponse.class)
            .retryWhen(createRetrySpec())
            .block();
    }

    public SpotifyArtist getArtist(String accessToken, String artistId) {
        try {
            return webClient.get()
                .uri("/artists/{id}", artistId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifyArtist.class)
                .retryWhen(createRetrySpec())
                .block();
        } catch (WebClientResponseException e) {
            log.warn("Erro ao buscar artista {}: {}", artistId, e.getMessage());
            return null;
        }
    }

    public Map<String, Object> refreshAccessToken(
        String refreshToken, 
        String clientId, 
        String clientSecret
    ) {
        try {
            return webClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, 
                    "Basic " + java.util.Base64.getEncoder()
                        .encodeToString((clientId + ":" + clientSecret).getBytes()))
                .bodyValue("grant_type=refresh_token&refresh_token=" + refreshToken)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        } catch (Exception e) {
            log.error("Erro ao renovar token: {}", e.getMessage());
            throw new SpotifyApiException("Falha ao renovar token do Spotify", e);
        }
    }

    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(1))
            .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
            .doBeforeRetry(retrySignal -> 
                log.warn("Retrying request due to rate limit, attempt: {}", 
                    retrySignal.totalRetries() + 1)
            );
    }
}