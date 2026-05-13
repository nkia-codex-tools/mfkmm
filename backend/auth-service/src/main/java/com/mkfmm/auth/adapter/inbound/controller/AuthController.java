package com.mkfmm.auth.adapter.inbound.controller;

import com.mkfmm.auth.adapter.inbound.dto.*;
import com.mkfmm.auth.application.port.inbound.AuthUseCase;
import com.mkfmm.shared.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        AuthResponse response = authUseCase.login(request.userId(), request.password(), ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
                                       @RequestHeader("X-User-Id") String userId) {
        authUseCase.logout(userId, request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request,
                                               @RequestHeader("X-User-Id") String userId) {
        authUseCase.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request,
                                              @RequestHeader("X-User-Id") String adminUserId) {
        authUseCase.resetPassword(request.targetUserId(), request.newPassword(), adminUserId);
        return ResponseEntity.noContent().build();
    }

    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
