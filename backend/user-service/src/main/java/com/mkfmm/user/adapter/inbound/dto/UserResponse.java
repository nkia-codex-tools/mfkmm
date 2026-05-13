package com.mkfmm.user.adapter.inbound.dto;

import com.mkfmm.user.domain.model.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String userId,
        String name,
        String email,
        String department,
        String role,
        String memo,
        boolean isLocked,
        boolean mustChangePassword,
        Instant createdAt,
        String createdBy,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getDepartment(),
                user.getRole().name(),
                user.getMemo(),
                user.isLocked(),
                user.isMustChangePassword(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                user.getUpdatedAt()
        );
    }
}
