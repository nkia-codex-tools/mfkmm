package com.resourcemanager.version.dto;

import jakarta.validation.constraints.NotBlank;

public record VersionTagRequest(
        @NotBlank(message = "Tag name is required")
        String tagName,
        String description,
        @NotBlank(message = "Resource type is required")
        String resourceType
) {}
