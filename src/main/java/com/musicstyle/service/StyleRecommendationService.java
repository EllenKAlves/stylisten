package com.musicstyle.service;

import com.musicstyle.dto.StyleRecommendationResponse;
import com.musicstyle.model.entity.User;
import com.musicstyle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StyleRecommendationService {

    private final SpotifyMusicAnalysisService musicAnalysisService;
    private final FashionStyleMatchingService matchingService;
    private final UserRepository userRepository;

    /**
     * Fluxo completo de recomendação de estilos
     */
    @Transactional
    public StyleRecommendationResponse generateRecommendations(String accessToken) {
        
        log.info("Iniciando análise de estilo musical...");

        // 1. Buscar top tracks do Spotify
        List<Map<String, Object>> topTracks = 
            musicAnalysisService.fetchRecentTopTracks(accessToken);
        
        if (topTracks.isEmpty()) {
            throw new IllegalStateException(
                "Nenhuma música encontrada no histórico recente"
            );
        }

        log.info("Encontradas {} tracks para análise", topTracks.size());

        // 2. Extrair e normalizar gêneros
        Map<String, Integer> rawGenres = 
            musicAnalysisService.extractGenreFrequency(topTracks, accessToken);
        
        Map<String, Integer> normalizedGenres = 
            musicAnalysisService.normalizeGenres(rawGenres);

        log.info("Perfil de gêneros: {}", normalizedGenres);

        // 3. Buscar usuário (você precisará obter o user do token)
        User user = getUserFromToken(accessToken);

        // 4. Salvar perfil musical
        matchingService.saveUserProfile(
            user, 
            normalizedGenres, 
            topTracks.size()
        );

        // 5. Calcular e retornar top 5 estilos
        StyleRecommendationResponse recommendations = 
            matchingService.findTopMatchingStyles(
                user,
                normalizedGenres,
                topTracks.size()
            );

        log.info("Top 5 estilos recomendados: {}", 
            recommendations.getTopStyles().stream()
                .map(StyleRecommendationResponse.StyleMatch::getStyleName)
                .toList()
        );

        return recommendations;
    }

    /**
     * Extrai usuário do access token (implementar conforme sua autenticação)
     */
    private User getUserFromToken(String accessToken) {
        // TODO: Implementar extração do usuário do token
        // Pode usar SpotifyUserService.fetchAndSaveSpotifyUser()
        throw new UnsupportedOperationException("Implementar extração de usuário");
    }
}