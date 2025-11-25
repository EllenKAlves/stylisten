package com.stylisten.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class SpotifyConnectRequest {
    @NotBlank(message = "returnUri é obrigatório")
    private String returnUri;
}

@Data @Builder
public class SpotifyConnectResponse {
    private String authUrl;
}

@Data @Builder
public class SpotifyCallbackRequest {
    @NotBlank(message = "code é obrigatório")
    private String code;
    
    private String state;
    
    @NotNull(message = "userId é obrigatório")
    private UUID userId;
}

@Data @Builder
public class SpotifyCallbackResponse {
    private String message;
    private UUID spotifyAccountId;
}