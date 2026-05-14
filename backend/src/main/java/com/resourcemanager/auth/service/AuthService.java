package com.resourcemanager.auth.service;

import com.resourcemanager.auth.dto.*;
import com.resourcemanager.auth.entity.Role;
import com.resourcemanager.auth.entity.Status;
import com.resourcemanager.auth.entity.User;
import com.resourcemanager.auth.repository.UserRepository;
import com.resourcemanager.auth.security.JwtTokenProvider;
import com.resourcemanager.common.exception.AuthenticationFailedException;
import com.resourcemanager.common.exception.ConflictException;
import com.resourcemanager.common.exception.AccountLockedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.email());
                    return new AuthenticationFailedException("Invalid credentials");
                });

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            long minutesRemaining = Duration.between(Instant.now(), user.getLockedUntil()).toMinutes() + 1;
            log.warn("Login failed - account locked: {}", user.getEmail());
            throw new AccountLockedException("Account is locked. Try again in " + minutesRemaining + " minutes.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            log.warn("Login failed - invalid password: {}, attempts: {}", user.getEmail(), user.getFailedLoginAttempts());
            throw new AuthenticationFailedException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        if (user.getStatus() == Status.LOCKED) {
            user.setStatus(user.getRole() == Role.PENDING ? Status.PENDING : Status.ACTIVE);
        }
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("Login successful: userId={}, email={}", user.getId(), user.getEmail());

        return new TokenResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiry(), UserResponse.from(user));
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setRole(Role.PENDING);
        user.setStatus(Status.PENDING);

        User saved = userRepository.save(user);
        log.info("User registered: userId={}, email={}", saved.getId(), saved.getEmail());

        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshRequest request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }

        var claims = jwtTokenProvider.getClaims(request.refreshToken());
        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new AuthenticationFailedException("Invalid token type");
        }

        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

        if (user.getStatus() == Status.LOCKED) {
            throw new AccountLockedException("Account is locked");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);

        return new TokenResponse(accessToken, request.refreshToken(), jwtTokenProvider.getAccessTokenExpiry(), UserResponse.from(user));
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
            user.setStatus(Status.LOCKED);
            log.warn("Account locked due to {} failed attempts: {}", MAX_FAILED_ATTEMPTS, user.getEmail());
        }
        userRepository.save(user);
    }
}
