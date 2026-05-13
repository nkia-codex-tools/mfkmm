package com.mkfmm.auth.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String userId,
    @NotBlank String password
) {}
