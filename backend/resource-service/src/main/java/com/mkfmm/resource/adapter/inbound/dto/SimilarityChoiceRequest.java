package com.mkfmm.resource.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record SimilarityChoiceRequest(
        @NotBlank String resourceKey,
        @NotBlank String resourceType,
        @NotBlank String content,
        String description,
        @NotBlank String chosenAction,
        String chosenResourceId,
        String reason
) {}
