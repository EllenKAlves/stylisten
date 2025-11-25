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
@RequestMapping("/api/v1/stylisten/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Geração e consulta de perfis musicais")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/generate")
    @Operation(summary = "Gera perfil musical e recomendações de estilo")
    public ResponseEntity<ProfileResponse> generateProfile(
        @Valid @RequestBody GenerateProfileRequest request
    ) {
        ProfileResponse response = profileService.generateProfile(
            request.getUserId(),
            request.getForceRefresh()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Consulta perfil gerado existente")
    public ResponseEntity<ProfileResponse> getProfile(
        @PathVariable UUID userId
    ) {
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
}