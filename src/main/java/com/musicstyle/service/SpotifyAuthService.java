package com.musicstyle.service;

import com.musicstyle.dto.SpotifyUserResponse;
import com.musicstyle.model.entity.User;
import com.musicstyle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpotifyAuthService {

    private final WebClient spotifyWebClient;
    private final UserRepository userRepository;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    private static final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";

    public String buildAuthorizationUrl() {
        return "https://accounts.spotify.com/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=user-read-email user-read-private";
    }

    public Map<String, Object> exchangeCodeForToken(String code) {
        WebClient webClient = WebClient.create(SPOTIFY_TOKEN_URL);

        Map<String, Object> response = webClient.post()
                .uri("")
                .header(h -> h.setBasicAuth(clientId, clientSecret))
                .bodyValue(Map.of(
                        "grant_type", "authorization_code",
                        "code", code,
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return response;
    }

    public Map<String, Object> refreshAccessToken(String refreshToken) {
        WebClient webClient = WebClient.create(SPOTIFY_TOKEN_URL);

        return webClient.post()
                .uri("")
                .header(h -> h.setBasicAuth(clientId, clientSecret))
                .bodyValue(Map.of(
                        "grant_type", "refresh_token",
                        "refresh_token", refreshToken
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

    }

    public record SpotifyTokenResponse(
        String access_token,
        String token_type,
        int expires_in,
        String refresh_token,
        String scope
    ) {}
}