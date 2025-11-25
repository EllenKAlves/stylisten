package com.stylisten.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GenreMappingResponse {
    private String genreName;
    private List<StyleWithWeight> styles;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StyleWithWeight {
    private UUID styleId;
    private String styleName;
    private Double weight;
}