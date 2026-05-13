package com.mkfmm.dataio.domain.model;

import java.time.Instant;

public record ExportResult(
    String fileName,
    FileFormat format,
    long fileSize,
    int totalRecords,
    byte[] data,
    Instant createdAt
) {}
