package com.stylist.controller;

import com.stylist.service.SpotifyUserService;
import com.stylist.service.SpotifyAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private SpotifyUserService spotifyUserService;
    
    @Autowired
    private SpotifyAuthService spotifyAuthService;

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok(spotifyAuthService.getAuthorizationUrl());
    }
    
   @GetMapping("/callback")
public ResponseEntity<String> callback(@RequestParam String code) {
    return ResponseEntity.ok(spotifyAuthService.getAccessToken(code));
    }
}