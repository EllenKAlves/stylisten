package com.stylisten.application.service;

import com.stylisten.api.dto.*;
import com.stylisten.domain.entity.SpotifyAccount;
import com.stylisten.domain.entity.User;
import com.stylisten.domain.repository.SpotifyAccountRepository;
import com.stylisten.domain.repository.UserRepository;
import com.stylisten.infrastructure.exception.ResourceNotFoundException;
import com.stylisten.infrastructure.spotify.SpotifyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SpotifyAccountRepository spotifyAccountRepository;
    private final UserRepository userRepository;
    private final SpotifyClient spotifyClient;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.spotify.scope}")
    private String scope;

    public SpotifyConnectResponse initiateSpotifyAuth(String returnUri) {
        log.info("Iniciando fluxo OAuth para Spotify");

        String authUrl = UriComponentsBuilder
            .fromUriString("https://accounts.spotify.com/authorize")
            .queryParam("client_id", clientId)
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", scope)
            .queryParam("state", UUID.randomUUID().toString())
            .queryParam("show_dialog", "true")
            .build()
            .toUriString();

        return SpotifyConnectResponse.builder()
            .authUrl(authUrl)
            .build();
    }

    @Transactional
    public SpotifyCallbackResponse handleCallback(SpotifyCallbackRequest request) {
        log.info("Processando callback do Spotify para usuário: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

    
        Map<String, Object> tokenResponse = exchangeCodeForToken(request.getCode());

        String accessToken = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        Integer expiresIn = (Integer) tokenResponse.get("expires_in");

        SpotifyUserProfile profile = spotifyClient.getUserProfile(accessToken);

        SpotifyAccount account = spotifyAccountRepository
            .findBySpotifyUserId(profile.getId())
            .orElse(SpotifyAccount.builder()
                .spotifyUserId(profile.getId())
                .user(user)
                .build());

        account.setAccessToken(accessToken);
        account.setRefreshToken(refreshToken);
        account.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));

        account = spotifyAccountRepository.save(account);

        log.info("Conta Spotify vinculada com sucesso: {}", account.getId());

        return SpotifyCallbackResponse.builder()
            .message("Spotify linked")
            .spotifyAccountId(account.getId())
            .build();
    }

    private Map<String, Object> exchangeCodeForToken(String code) {
        try {
            return spotifyClient.refreshAccessToken(code, clientId, clientSecret);
        } catch (Exception e) {
            log.error("Erro ao trocar code por token: {}", e.getMessage());
            throw new RuntimeException("Falha ao autenticar com Spotify", e);
        }
    }

    @Transactional
    public void refreshTokenIfNeeded(UUID userId) {
        SpotifyAccount account = spotifyAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conta Spotify não encontrada"));

        Instant now = Instant.now();
        Instant expiresAt = account.getTokenExpiresAt();

        //renova token se expirou ou ta perto (5min de margem)
        if (expiresAt == null || expiresAt.isBefore(now.plusSeconds(300))) {
            log.info("Renovando token do Spotify para usuário: {}", userId);

            Map<String, Object> tokenResponse = spotifyClient.refreshAccessToken(
                account.getRefreshToken(),
                clientId,
                clientSecret
            );

            String newAccessToken = (String) tokenResponse.get("access_token");
            Integer expiresIn = (Integer) tokenResponse.get("expires_in");

            account.setAccessToken(newAccessToken);
            account.setTokenExpiresAt(now.plusSeconds(expiresIn));

            //atualiza refresh token(se fornecido)
            if (tokenResponse.containsKey("refresh_token")) {
                String newRefreshToken = (String) tokenResponse.get("refresh_token");
                account.setRefreshToken(newRefreshToken);
            }

            spotifyAccountRepository.save(account);
        }
    }
}