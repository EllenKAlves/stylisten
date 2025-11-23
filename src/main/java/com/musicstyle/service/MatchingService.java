package com.musicstyle.service;

import com.musicstyle.dto.StyleRecommendationResponse;
import com.musicstyle.dto.StyleRecommendationResponse.StyleMatch;
import com.musicstyle.model.entity.FashionStyle;
import com.musicstyle.model.entity.User;
import com.musicstyle.model.entity.UserMusicProfile;
import com.musicstyle.repository.FashionStyleRepository;
import com.musicstyle.repository.UserMusicProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FashionStyleMatchingService {

    private final FashionStyleRepository fashionStyleRepository;
    private final UserMusicProfileRepository profileRepository;

    /**
     * Calcula o score de compatibilidade entre perfil musical e estilo de moda
     * 
     * Algoritmo:
     * 1. Para cada gênero do usuário, verifica se está associado ao estilo
     * 2. Pontuação = (frequência do gênero * peso do estilo) / total de tracks
     * 3. Bônus se múltiplos gêneros do usuário coincidem com o estilo
     */
    public double calculateMatchScore(
            Map<String, Integer> userGenres,
            FashionStyle style,
            int totalTracks) {
        
        double score = 0.0;
        int matchingGenresCount = 0;
        List<String> styleGenres = style.getAssociatedMusicGenres()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        for (Map.Entry<String, Integer> entry : userGenres.entrySet()) {
            String userGenre = entry.getKey().toLowerCase();
            Integer frequency = entry.getValue();

            // Verifica match exato ou parcial (contains)
            boolean matches = styleGenres.stream()
                .anyMatch(sg -> 
                    sg.equals(userGenre) || 
                    sg.contains(userGenre) || 
                    userGenre.contains(sg)
                );

            if (matches) {
                matchingGenresCount++;
                // Pontuação baseada na frequência normalizada
                double genreScore = (frequency * 100.0) / totalTracks;
                score += genreScore * (style.getWeight() / 100.0);
            }
        }

        // Bônus de diversidade: múltiplos gêneros matching
        if (matchingGenresCount > 1) {
            score *= (1 + (matchingGenresCount * 0.1)); // +10% por gênero adicional
        }

        // Limita score a 100
        return Math.min(score, 100.0);
    }

    /**
     * Gera explicação do porquê o estilo foi recomendado
     */
    private String generateReasoning(
            Map<String, Integer> userGenres,
            FashionStyle style,
            List<String> matchingGenres) {
        
        if (matchingGenres.isEmpty()) {
            return "Baseado no seu perfil musical geral";
        }

        String topGenres = matchingGenres.stream()
            .limit(3)
            .collect(Collectors.joining(", "));

        return String.format(
            "Você escuta %s, que combina perfeitamente com o estilo %s",
            topGenres,
            style.getName().toLowerCase()
        );
    }

    /**
     * Encontra os top 5 estilos mais compatíveis
     */
    @Transactional(readOnly = true)
    public StyleRecommendationResponse findTopMatchingStyles(
            User user,
            Map<String, Integer> userGenres,
            int totalTracks) {
        
        List<FashionStyle> allStyles = fashionStyleRepository.findAll();
        
        List<StyleMatch> rankedStyles = allStyles.stream()
            .map(style -> {
                double score = calculateMatchScore(userGenres, style, totalTracks);
                
                List<String> matchingGenres = findMatchingGenres(
                    userGenres.keySet(), 
                    style.getAssociatedMusicGenres()
                );

                return StyleMatch.builder()
                    .styleName(style.getName())
                    .description(style.getDescription())
                    .matchScore(Math.round(score * 100.0) / 100.0)
                    .matchingGenres(matchingGenres)
                    .imageUrl(style.getImageUrl())
                    .reasoning(generateReasoning(userGenres, style, matchingGenres))
                    .build();
            })
            .sorted(Comparator.comparingDouble(StyleMatch::getMatchScore).reversed())
            .limit(5)
            .collect(Collectors.toList());

        return StyleRecommendationResponse.builder()
            .userId(user.getSpotifyId())
            .topStyles(rankedStyles)
            .userGenreProfile(userGenres)
            .totalTracksAnalyzed(totalTracks)
            .analyzedPeriod("last_month")
            .build();
    }

    /**
     * Encontra gêneros em comum entre usuário e estilo
     */
    private List<String> findMatchingGenres(
            Set<String> userGenres,
            Set<String> styleGenres) {
        
        return userGenres.stream()
            .filter(ug -> styleGenres.stream()
                .anyMatch(sg -> 
                    sg.equalsIgnoreCase(ug) ||
                    sg.toLowerCase().contains(ug.toLowerCase()) ||
                    ug.toLowerCase().contains(sg.toLowerCase())
                ))
            .collect(Collectors.toList());
    }

    /**
     * Salva o perfil musical do usuário para histórico
     */
    @Transactional
    public UserMusicProfile saveUserProfile(
            User user,
            Map<String, Integer> genreFrequency,
            int totalTracks) {
        
        UserMusicProfile profile = UserMusicProfile.builder()
            .user(user)
            .genreFrequency(genreFrequency)
            .totalTracksAnalyzed(totalTracks)
            .build();

        return profileRepository.save(profile);
    }
}