package com.stylisten.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
class SpotifyRecentlyPlayedResponse {
    private List<SpotifyPlayHistoryItem> items;
    
    @JsonProperty("next")
    private String nextUrl;
}

@Data
class SpotifyPlayHistoryItem {
    private SpotifyTrack track;
    
    @JsonProperty("played_at")
    private String playedAt;
}

@Data
class SpotifyTrack {
    private String id;
    private String name;
    private List<SpotifyArtist> artists;
}

@Data
class SpotifyArtist {
    private String id;
    private String name;
    private List<String> genres;
}

@Data
class SpotifyUserProfile {
    private String id;
    private String email;
    
    @JsonProperty("display_name")
    private String displayName;
}