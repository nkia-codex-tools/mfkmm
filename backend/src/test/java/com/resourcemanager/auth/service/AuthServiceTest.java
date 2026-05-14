package com.resourcemanager.auth.service;

import com.resourcemanager.auth.dto.LoginRequest;
import com.resourcemanager.auth.dto.RegisterRequest;
import com.resourcemanager.auth.entity.Role;
import com.resourcemanager.auth.entity.Status;
import com.resourcemanager.auth.entity.User;
import com.resourcemanager.auth.repository.UserRepository;
import com.resourcemanager.auth.security.JwtTokenProvider;
import com.resourcemanager.common.exception.AccountLockedException;
import com.resourcemanager.common.exception.AuthenticationFailedException;
import com.resourcemanager.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setName("Test User");
        testUser.setRole(Role.WRITER);
        testUser.setStatus(Status.ACTIVE);
        testUser.setFailedLoginAttempts(0);
    }

    @Test
    void login_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiry()).thenReturn(900000L);

        var response = authService.login(new LoginRequest("test@example.com", "password"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }

    @Test
    void login_invalidPassword_incrementsFailedAttempts() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrong")))
                .isInstanceOf(AuthenticationFailedException.class);

        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository).save(testUser);
    }

    @Test
    void login_fiveFailedAttempts_locksAccount() {
        testUser.setFailedLoginAttempts(4);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrong")))
                .isInstanceOf(AuthenticationFailedException.class);

        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(testUser.getLockedUntil()).isNotNull();
        assertThat(testUser.getStatus()).isEqualTo(Status.LOCKED);
    }

    @Test
    void login_lockedAccount_throwsAccountLocked() {
        testUser.setLockedUntil(Instant.now().plus(10, ChronoUnit.MINUTES));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "password")))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void login_userNotFound_throwsAuthenticationFailed() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@example.com", "password")))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        var response = authService.register(new RegisterRequest("New User", "new@example.com", "password123"));

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.role()).isEqualTo("PENDING");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("User", "existing@example.com", "password123")))
                .isInstanceOf(ConflictException.class);
    }
}
