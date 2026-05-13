package com.mkfmm.dataio.adapter.inbound.dto;

import com.mkfmm.dataio.domain.model.ImportError;
import com.mkfmm.dataio.domain.model.ImportJob;

import java.util.List;

public record ImportResponse(
    String jobId,
    int totalRows,
    int successCount,
    int failedCount,
    int skippedCount,
    List<ImportError> errors
) {
    public static ImportResponse from(ImportJob job) {
        return new ImportResponse(
            job.getId(),
            job.getTotalRows(),
            job.getSuccessCount(),
            job.getFailedCount(),
            job.getSkippedCount(),
            job.getErrors()
        );
    }
}
