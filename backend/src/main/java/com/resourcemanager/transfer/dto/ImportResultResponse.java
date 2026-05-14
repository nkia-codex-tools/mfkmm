package com.resourcemanager.transfer.dto;

public record ImportResultResponse(
        int created,
        int updated,
        int skipped,
        int failed
) {}
