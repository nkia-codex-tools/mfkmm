package com.mkfmm.shared.dto;

public record UserInfo(
    String id,
    String userId,
    String role,
    boolean isLocked
) {}
