package com.stylist.controller;

import com.stylist.service.StyleAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/style")
@RequiredArgsConstructor
public class StyleController {

    private final StyleAnalysisService styleService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeStyle(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token inválido");
        }
        return ResponseEntity.ok(styleService.generateStyleProfile(token.replace("Bearer ", "")));
    }
}
