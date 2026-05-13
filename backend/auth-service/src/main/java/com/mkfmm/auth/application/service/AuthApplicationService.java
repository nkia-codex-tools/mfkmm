package com.mkfmm.auth.application.service;

import com.mkfmm.auth.application.port.inbound.AuthUseCase;
import com.mkfmm.auth.application.port.outbound.*;
import com.mkfmm.auth.domain.model.*;
import com.mkfmm.auth.domain.service.PasswordService;
import com.mkfmm.shared.dto.AuthResponse;
import com.mkfmm.shared.dto.UserInfo;
import com.mkfmm.shared.event.AuthEvents;
import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.AuthenticationException;
import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthApplicationService implements AuthUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final AccountLockEventRepository accountLockEventRepository;
    private final EventPublisherPort eventPublisher;
    private final PasswordService passwordService;
    private final JwtTokenService jwtTokenService;

    public AuthApplicationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            LoginHistoryRepository loginHistoryRepository,
            AccountLockEventRepository accountLockEventRepository,
            EventPublisherPort eventPublisher,
            PasswordService passwordService,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.accountLockEventRepository = accountLockEventRepository;
        this.eventPublisher = eventPublisher;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public AuthResponse login(String userId, String password, String ipAddress) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    loginHistoryRepository.save(LoginHistory.failure(userId, ipAddress, "USER_NOT_FOUND"));
                    return new AuthenticationException("AUTH_001", "Invalid credentials");
                });

        if (user.isLocked()) {
            loginHistoryRepository.save(LoginHistory.failure(userId, ipAddress, "ACCOUNT_LOCKED"));
            throw new AuthenticationException("AUTH_002", "Account is locked");
        }

        if (!passwordService.matches(password, user.getPasswordHash())) {
            user.incrementFailedAttempts();
            if (user.shouldLock()) {
                user.lock();
                userRepository.save(user);
                AccountLockEvent lockEvent = AccountLockEvent.locked(userId);
                accountLockEventRepository.save(lockEvent);
                eventPublisher.publish(BaseEvent.of(
                        AuthEvents.ACCOUNT_LOCKED, userId,
                        new AuthEvents.AccountLockedPayload(userId, Instant.now(), user.getFailedLoginAttempts())
                ));
            } else {
                userRepository.save(user);
            }
            loginHistoryRepository.save(LoginHistory.failure(userId, ipAddress, "INVALID_PASSWORD"));
            throw new AuthenticationException("AUTH_001", "Invalid credentials");
        }

        user.resetFailedAttempts();
        userRepository.save(user);

        String accessToken = jwtTokenService.generateAccessToken(user);
        RefreshToken refreshToken = new RefreshToken(userId, jwtTokenService.getRefreshTokenTtl());
        refreshTokenRepository.save(refreshToken);

        loginHistoryRepository.save(LoginHistory.success(userId, ipAddress));
        eventPublisher.publish(BaseEvent.of(
                AuthEvents.USER_LOGGED_IN, userId,
                new AuthEvents.UserLoggedInPayload(userId, ipAddress, Instant.now())
        ));

        UserInfo userInfo = new UserInfo(user.getId(), user.getUserId(), user.getRole().name(), user.isLocked());
        return new AuthResponse(accessToken, refreshToken.getToken(), jwtTokenService.getAccessTokenTtl(), userInfo);
    }

    @Override
    public void logout(String userId, String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });

        eventPublisher.publish(BaseEvent.of(
                AuthEvents.USER_LOGGED_OUT, userId,
                new AuthEvents.UserLoggedOutPayload(userId, Instant.now())
        ));
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("AUTH_003", "Invalid refresh token"));

        if (!token.isValid()) {
            throw new AuthenticationException("AUTH_006", "Refresh token is revoked or expired");
        }

        User user = userRepository.findByUserId(token.getUserId())
                .orElseThrow(() -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                    return new AuthenticationException("AUTH_004", "User not found");
                });

        if (user.isLocked()) {
            token.revoke();
            refreshTokenRepository.save(token);
            throw new AuthenticationException("AUTH_002", "Account is locked");
        }

        String accessToken = jwtTokenService.generateAccessToken(user);
        UserInfo userInfo = new UserInfo(user.getId(), user.getUserId(), user.getRole().name(), user.isLocked());
        return new AuthResponse(accessToken, refreshToken, jwtTokenService.getAccessTokenTtl(), userInfo);
    }

    @Override
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("COMMON_002", "User not found"));

        if (!passwordService.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("AUTH_001", "Current password is incorrect");
        }

        user.changePassword(passwordService.hash(newPassword));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId);

        eventPublisher.publish(BaseEvent.of(
                AuthEvents.PASSWORD_CHANGED, userId,
                new AuthEvents.PasswordChangedPayload(userId, userId, Instant.now())
        ));
    }

    @Override
    public void resetPassword(String targetUserId, String newPassword, String adminUserId) {
        User admin = userRepository.findByUserId(adminUserId)
                .orElseThrow(() -> new BusinessException("COMMON_002", "Admin not found"));

        if (!admin.getRole().isAdminOrAbove()) {
            throw new ForbiddenException("Insufficient permissions");
        }

        User target = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new BusinessException("COMMON_002", "Target user not found"));

        if (target.isRootAdmin()) {
            throw new BusinessException("AUTH_007", "Cannot reset ROOT_ADMIN password");
        }

        target.changePassword(passwordService.hash(newPassword));
        userRepository.save(target);
        refreshTokenRepository.revokeAllByUserId(targetUserId);

        eventPublisher.publish(BaseEvent.of(
                AuthEvents.PASSWORD_CHANGED, targetUserId,
                new AuthEvents.PasswordChangedPayload(targetUserId, adminUserId, Instant.now())
        ));
    }
}
