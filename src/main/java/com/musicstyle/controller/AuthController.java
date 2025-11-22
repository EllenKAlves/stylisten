package com.musicstyle.controller;

import com.musicstyle.service.SpotifyAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity; 

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SpotifyAuthService spotifyAuthService;

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        String loginUrl = authService.buildAuthorizationUrl();
        return ResponseEntity.ok(loginUrl);
    }


    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        return ResponseEntity.ok(authService.exchangeCodeForToken(code));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestParam("refreshToken") String refreshToken) {
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }
}