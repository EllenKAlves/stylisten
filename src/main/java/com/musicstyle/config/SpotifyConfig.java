package com.musicstyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SpotifyClientConfig {

    private static final String SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1";

    @Bean
    public WebClient spotifyWebClient() {
        return WebClient.builder()
                .baseUrl(SPOTIFY_API_BASE_URL)
                .build();
    }
}

