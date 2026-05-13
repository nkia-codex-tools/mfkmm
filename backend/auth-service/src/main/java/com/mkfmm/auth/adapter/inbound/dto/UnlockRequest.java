package com.mkfmm.auth.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record UnlockRequest(
    @NotBlank String targetUserId
) {}
