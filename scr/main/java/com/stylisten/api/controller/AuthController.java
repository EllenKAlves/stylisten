package com.stylisten.api.controller;

import com.stylisten.api.dto.*;
import com.stylisten.application.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticação Spotify")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/spotify/connect")
    @Operation(summary = "Inicia fluxo OAuth com Spotify")
    public ResponseEntity<SpotifyConnectResponse> connectSpotify(
        @Valid @RequestBody SpotifyConnectRequest request
    ) {
        SpotifyConnectResponse response = authService.initiateSpotifyAuth(
            request.getReturnUri()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/spotify/callback")
    @Operation(summary = "Callback OAuth Spotify")
    public ResponseEntity<SpotifyCallbackResponse> callbackSpotify(
        @Valid @RequestBody SpotifyCallbackRequest request
    ) {
        SpotifyCallbackResponse response = authService.handleCallback(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}