package com.mkfmm.auth.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
    @NotBlank String currentPassword,
    @NotBlank String newPassword
) {}
