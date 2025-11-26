package com.stylisten.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylisten.api.dto.*;
import com.stylisten.domain.entity.GenreStyleMapping;
import com.stylisten.domain.entity.Style;
import com.stylisten.domain.repository.GenreStyleMappingRepository;
import com.stylisten.domain.repository.StyleRepository;
import com.stylisten.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StyleService {

    private final StyleRepository styleRepository;
    private final GenreStyleMappingRepository mappingRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public StyleListResponse getAllStyles() {
        List<Style> styles = styleRepository.findAll();
        
        List<StyleResponse> responses = styles.stream()
            .map(this::convertToResponse)
            .toList();

        return StyleListResponse.builder()
            .styles(responses)
            .total(responses.size())
            .build();
    }

    @Transactional(readOnly = true)
    public StyleResponse getStyleById(UUID styleId) {
        Style style = styleRepository.findById(styleId)
            .orElseThrow(() -> new ResourceNotFoundException("Estilo não encontrado"));

        return convertToResponse(style);
    }

    @Transactional
    public StyleResponse createStyle(StyleRequest request) {
        log.info("Criando novo estilo: {}", request.getName());

        Style style = Style.builder()
            .name(request.getName())
            .description(request.getDescription())
            .tags(request.getTags() != null ? 
                request.getTags().toArray(new String[0]) : new String[0])
            .build();

        if (request.getExampleImages() != null && !request.getExampleImages().isEmpty()) {
            try {
                style.setExampleImages(
                    objectMapper.writeValueAsString(request.getExampleImages())
                );
            } catch (Exception e) {
                log.error("Erro ao serializar imagens: {}", e.getMessage());
            }
        }

        style = styleRepository.save(style);

        //criando mapeamento
        if (request.getGenreMappings() != null) {
            List<GenreStyleMapping> mappings = request.getGenreMappings().stream()
                .map(m -> GenreStyleMapping.builder()
                    .genreName(m.getGenreName().toLowerCase())
                    .style(style)
                    .weight(m.getWeight())
                    .build())
                .toList();

            mappingRepository.saveAll(mappings);
        }

        log.info("Estilo criado com sucesso: {}", style.getId());
        return convertToResponse(style);
    }

    @Transactional
    public StyleResponse updateStyle(UUID styleId, StyleRequest request) {
        log.info("Atualizando estilo: {}", styleId);

        Style style = styleRepository.findById(styleId)
            .orElseThrow(() -> new ResourceNotFoundException("Estilo não encontrado"));

        style.setName(request.getName());
        style.setDescription(request.getDescription());
        style.setTags(request.getTags() != null ? 
            request.getTags().toArray(new String[0]) : new String[0]);

        if (request.getExampleImages() != null) {
            try {
                style.setExampleImages(
                    objectMapper.writeValueAsString(request.getExampleImages())
                );
            } catch (Exception e) {
                log.error("Erro ao serializar imagens: {}", e.getMessage());
            }
        }

        //remove mapeamentos antigos e faz novos, vai q a pessoa mudou ne
        if (request.getGenreMappings() != null) {
            List<GenreStyleMapping> existingMappings = 
                mappingRepository.findByGenreNameIn(
                    request.getGenreMappings().stream()
                        .map(GenreMappingRequest::getGenreName)
                        .toList()
                );

            mappingRepository.deleteAll(existingMappings);

            List<GenreStyleMapping> newMappings = request.getGenreMappings().stream()
                .map(m -> GenreStyleMapping.builder()
                    .genreName(m.getGenreName().toLowerCase())
                    .style(style)
                    .weight(m.getWeight())
                    .build())
                .toList();

            mappingRepository.saveAll(newMappings);
        }

        style = styleRepository.save(style);
        return convertToResponse(style);
    }

    @Transactional
    public void deleteStyle(UUID styleId) {
        log.info("Deletando estilo: {}", styleId);

        Style style = styleRepository.findById(styleId)
            .orElseThrow(() -> new ResourceNotFoundException("Estilo não encontrado"));

        styleRepository.delete(style);
    }

    @Transactional(readOnly = true)
    public GenreMappingResponse getStylesByGenre(String genreName) {
        List<GenreStyleMapping> mappings = mappingRepository
            .findByGenreName(genreName.toLowerCase());

        List<StyleWithWeight> styles = mappings.stream()
            .sorted(Comparator.comparing(GenreStyleMapping::getWeight).reversed())
            .map(m -> StyleWithWeight.builder()
                .styleId(m.getStyle().getId())
                .styleName(m.getStyle().getName())
                .weight(m.getWeight())
                .build())
            .toList();

        return GenreMappingResponse.builder()
            .genreName(genreName)
            .styles(styles)
            .build();
    }

    private StyleResponse convertToResponse(Style style) {
        List<String> images = new ArrayList<>();
        
        if (style.getExampleImages() != null) {
            try {
                images = objectMapper.readValue(
                    style.getExampleImages(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                );
            } catch (Exception e) {
                log.warn("Erro ao deserializar imagens: {}", e.getMessage());
            }
        }

        return StyleResponse.builder()
            .id(style.getId())
            .name(style.getName())
            .description(style.getDescription())
            .tags(style.getTags() != null ? 
                Arrays.asList(style.getTags()) : Collections.emptyList())
            .exampleImages(images)
            .createdAt(style.getCreatedAt())
            .updatedAt(style.getUpdatedAt())
            .build();
    }
}