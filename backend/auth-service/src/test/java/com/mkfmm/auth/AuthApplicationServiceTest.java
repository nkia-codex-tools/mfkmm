package com.mkfmm.auth;

import com.mkfmm.auth.application.port.outbound.*;
import com.mkfmm.auth.application.service.AuthApplicationService;
import com.mkfmm.auth.application.service.JwtTokenService;
import com.mkfmm.auth.domain.model.*;
import com.mkfmm.auth.domain.service.PasswordService;
import com.mkfmm.shared.dto.AuthResponse;
import com.mkfmm.shared.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private AccountLockEventRepository accountLockEventRepository;
    @Mock private EventPublisherPort eventPublisher;
    @Mock private JwtTokenService jwtTokenService;

    private PasswordService passwordService;
    private AuthApplicationService authService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
        authService = new AuthApplicationService(
                userRepository, refreshTokenRepository, loginHistoryRepository,
                accountLockEventRepository, eventPublisher, passwordService, jwtTokenService);
    }

    @Test
    void loginSuccess() {
        User user = new User("admin", passwordService.hash("password123"), Role.ADMIN, false);
        user.setId("id-1");
        when(userRepository.findByUserId("admin")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenService.getAccessTokenTtl()).thenReturn(900L);
        when(jwtTokenService.getRefreshTokenTtl()).thenReturn(604800L);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.login("admin", "password123", "127.0.0.1");

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        verify(loginHistoryRepository).save(any(LoginHistory.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void loginFailUserNotFound() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authService.login("unknown", "pass", "127.0.0.1"));
    }

    @Test
    void loginFailAccountLocked() {
        User user = new User("locked", passwordService.hash("pass"), Role.READ, false);
        user.lock();
        when(userRepository.findByUserId("locked")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class,
                () -> authService.login("locked", "pass", "127.0.0.1"));
    }

    @Test
    void loginFailWrongPassword() {
        User user = new User("admin", passwordService.hash("correct"), Role.ADMIN, false);
        when(userRepository.findByUserId("admin")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThrows(AuthenticationException.class,
                () -> authService.login("admin", "wrong", "127.0.0.1"));
        assertEquals(1, user.getFailedLoginAttempts());
    }

    @Test
    void loginFailTriggersLock() {
        User user = new User("admin", passwordService.hash("correct"), Role.ADMIN, false);
        user.setFailedLoginAttempts(2);
        when(userRepository.findByUserId("admin")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountLockEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThrows(AuthenticationException.class,
                () -> authService.login("admin", "wrong", "127.0.0.1"));
        assertTrue(user.isLocked());
        verify(eventPublisher).publish(any());
    }

    @Test
    void logoutIdempotent() {
        when(refreshTokenRepository.findByToken("token-123")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.logout("admin", "token-123"));
        verify(eventPublisher).publish(any());
    }

    @Test
    void refreshSuccess() {
        RefreshToken token = new RefreshToken("admin", 604800);
        when(refreshTokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));
        User user = new User("admin", "hash", Role.ADMIN, false);
        user.setId("id-1");
        when(userRepository.findByUserId("admin")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateAccessToken(any())).thenReturn("new-access");
        when(jwtTokenService.getAccessTokenTtl()).thenReturn(900L);

        AuthResponse response = authService.refresh(token.getToken());

        assertEquals("new-access", response.accessToken());
    }
}
