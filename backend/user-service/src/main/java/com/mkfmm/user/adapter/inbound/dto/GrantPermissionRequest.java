package com.mkfmm.user.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record GrantPermissionRequest(
        @NotBlank String role
) {}
