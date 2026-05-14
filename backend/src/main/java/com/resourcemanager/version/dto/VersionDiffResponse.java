package com.resourcemanager.version.dto;

public record VersionDiffResponse(
        Long versionId,
        String tagName,
        String resourceType,
        int snapshotRowCount,
        int currentRowCount,
        String snapshotPreview
) {}
