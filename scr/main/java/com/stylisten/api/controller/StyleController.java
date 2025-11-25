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
@RequestMapping("/api/v1/styles")
@RequiredArgsConstructor
@Tag(name = "Styles", description = "CRUD de estilos de moda")
public class StyleController {

    private final StyleService styleService;

    @GetMapping
    @Operation(summary = "Lista todos os estilos")
    public ResponseEntity<StyleListResponse> getAllStyles() {
        StyleListResponse response = styleService.getAllStyles();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{styleId}")
    @Operation(summary = "Detalha um estilo específico")
    public ResponseEntity<StyleResponse> getStyle(
        @PathVariable UUID styleId
    ) {
        StyleResponse response = styleService.getStyleById(styleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Cria novo estilo (admin)")
    public ResponseEntity<StyleResponse> createStyle(
        @Valid @RequestBody StyleRequest request
    ) {
        StyleResponse response = styleService.createStyle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{styleId}")
    @Operation(summary = "Atualiza estilo existente (admin)")
    public ResponseEntity<StyleResponse> updateStyle(
        @PathVariable UUID styleId,
        @Valid @RequestBody StyleRequest request
    ) {
        StyleResponse response = styleService.updateStyle(styleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{styleId}")
    @Operation(summary = "Deleta estilo (admin)")
    public ResponseEntity<Void> deleteStyle(
        @PathVariable UUID styleId
    ) {
        styleService.deleteStyle(styleId);
        return ResponseEntity.noContent().build();
    }
}