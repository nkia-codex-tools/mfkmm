package com.mkfmm.auth.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
    @NotBlank String targetUserId,
    @NotBlank String newPassword
) {}
