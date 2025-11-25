package com.stylisten.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class StyleRequest {
    @NotBlank(message = "name é obrigatório")
    private String name;
    
    private String description;
    private List<String> tags;
    private List<GenreMappingRequest> genreMappings;
    private List<String> exampleImages;
}

@Data @Builder
public class GenreMappingRequest {
    @NotBlank
    private String genreName;
    
    @Min(0) @Max(1)
    private Double weight;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StyleResponse {
    private UUID id;
    private String name;
    private String description;
    private List<String> tags;
    private List<String> exampleImages;
    private Instant createdAt;
    private Instant updatedAt;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StyleListResponse {
    private List<StyleResponse> styles;
    private Integer total;
}