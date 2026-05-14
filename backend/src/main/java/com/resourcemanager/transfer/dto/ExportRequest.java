package com.resourcemanager.transfer.dto;

import java.util.List;

public record ExportRequest(
        List<Long> selectedIds
) {}
