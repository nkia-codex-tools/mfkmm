package com.mkfmm.shared.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    UserInfo user
) {}
