package com.resourcemanager.transfer.dto;

import java.util.List;
import java.util.Map;

public record ImportPreviewResponse(
        int totalRows,
        int validRows,
        int invalidRows,
        List<String> errors,
        List<Map<String, String>> previewData
) {}
