package com.resourcemanager.auth.dto;

import com.resourcemanager.auth.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String name,
        String role,
        String status,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
