package com.mkfmm.resource.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateResourceRequest(
        @NotBlank String resourceKey,
        @NotBlank String resourceType,
        @NotBlank String content,
        String description
) {}
