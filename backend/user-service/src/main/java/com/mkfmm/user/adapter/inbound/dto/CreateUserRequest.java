package com.mkfmm.user.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9]+$") String userId,
        @NotBlank String name,
        String email,
        String department,
        @NotBlank String role,
        @NotBlank String password,
        String memo
) {}
