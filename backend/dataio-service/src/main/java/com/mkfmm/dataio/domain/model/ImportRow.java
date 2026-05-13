package com.mkfmm.dataio.domain.model;

public record ImportRow(
    int rowNumber,
    ResourceType resourceType,
    String key,
    String content,
    String description
) {}
