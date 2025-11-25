package com.stylisten.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class ErrorResponse {
    private Integer status;
    private String message;
    private String path;
    private Instant timestamp;
    private List<String> errors;
}