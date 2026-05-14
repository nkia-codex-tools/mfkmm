package com.resourcemanager.version.dto;

import com.resourcemanager.version.entity.VersionTag;

import java.time.Instant;

public record VersionResponse(
        Long id,
        String tagName,
        String description,
        String resourceType,
        Long createdBy,
        Instant createdAt
) {
    public static VersionResponse from(VersionTag entity) {
        return new VersionResponse(
                entity.getId(), entity.getTagName(), entity.getDescription(),
                entity.getResourceType(), entity.getCreatedBy(), entity.getCreatedAt()
        );
    }
}
