package com.mkfmm.resource.adapter.inbound.dto;

import com.mkfmm.resource.domain.model.Resource;

import java.time.Instant;

public record ResourceResponse(
        String id,
        String resourceKey,
        String resourceType,
        String content,
        String description,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt
) {
    public static ResourceResponse from(Resource r) {
        return new ResourceResponse(
                r.getId(), r.getResourceKey(), r.getResourceType().name(),
                r.getContent(), r.getDescription(),
                r.getCreatedBy(), r.getCreatedAt(),
                r.getUpdatedBy(), r.getUpdatedAt());
    }
}
