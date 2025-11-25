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
@RequestMapping("/api/v1/mapping")
@RequiredArgsConstructor
@Tag(name = "Mapping", description = "Mapeamento gênero ↔ estilo")
public class MappingController {

    private final StyleService styleService;

    @GetMapping("/genre/{genreName}")
    @Operation(summary = "Busca estilos relacionados a um gênero")
    public ResponseEntity<GenreMappingResponse> getStylesByGenre(
        @PathVariable String genreName
    ) {
        GenreMappingResponse response = styleService.getStylesByGenre(genreName);
        return ResponseEntity.ok(response);
    }
}