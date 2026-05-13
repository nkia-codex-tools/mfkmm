package com.mkfmm.auth.application.port.inbound;

import com.mkfmm.shared.dto.AuthResponse;

public interface AuthUseCase {
    AuthResponse login(String userId, String password, String ipAddress);
    void logout(String userId, String refreshToken);
    AuthResponse refresh(String refreshToken);
    void changePassword(String userId, String currentPassword, String newPassword);
    void resetPassword(String targetUserId, String newPassword, String adminUserId);
}
