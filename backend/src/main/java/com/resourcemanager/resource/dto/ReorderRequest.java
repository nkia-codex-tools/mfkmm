package com.resourcemanager.resource.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReorderRequest(
        @NotEmpty(message = "Ordered IDs are required")
        List<Long> orderedIds
) {}
