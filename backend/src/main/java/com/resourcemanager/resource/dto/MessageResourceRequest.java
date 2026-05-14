package com.resourcemanager.resource.dto;

import jakarta.validation.constraints.Size;

public record MessageResourceRequest(
        @Size(max = 100) String module,
        @Size(max = 255) String resourceKey,
        @Size(max = 1000) String korean,
        @Size(max = 1000) String english,
        @Size(max = 1000) String japanese,
        @Size(max = 2000) String description,
        @Size(max = 20) String registeredDate,
        @Size(max = 100) String registeredBy
) {}
