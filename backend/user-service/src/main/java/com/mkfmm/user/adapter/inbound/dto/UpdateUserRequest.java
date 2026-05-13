package com.mkfmm.user.adapter.inbound.dto;

public record UpdateUserRequest(
        String name,
        String email,
        String department,
        String memo
) {}
