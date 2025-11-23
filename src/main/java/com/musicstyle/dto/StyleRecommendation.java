package com.musicstyle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleRecommendationResponse {
    private String userId;
    private List<StyleMatch> topStyles;
    private Map<String, Integer> userGenreProfile;
    private Integer totalTracksAnalyzed;
    private String analyzedPeriod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StyleMatch {
        private String styleName;
        private String description;
        private Double matchScore; // 0-100
        private List<String> matchingGenres;
        private String imageUrl;
        private String reasoning; // Explicação do match
    }
}