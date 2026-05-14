package com.resourcemanager.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(
        @NotBlank(message = "Role is required")
        String role
) {}
