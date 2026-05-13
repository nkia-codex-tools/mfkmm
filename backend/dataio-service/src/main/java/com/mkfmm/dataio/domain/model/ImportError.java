package com.mkfmm.dataio.domain.model;

public record ImportError(
    int rowNumber,
    String field,
    String value,
    String reason
) {}
