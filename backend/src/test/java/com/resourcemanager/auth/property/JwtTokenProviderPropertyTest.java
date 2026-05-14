package com.resourcemanager.auth.property;

import com.resourcemanager.auth.entity.Role;
import com.resourcemanager.auth.entity.Status;
import com.resourcemanager.auth.entity.User;
import com.resourcemanager.auth.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderPropertyTest {

    private JwtTokenProvider createProvider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret", "test-secret-key-that-is-at-least-32-characters-long");
        ReflectionTestUtils.setField(provider, "accessTokenExpiry", 900000L);
        ReflectionTestUtils.setField(provider, "refreshTokenExpiry", 604800000L);
        provider.init();
        return provider;
    }

    @Property(tries = 100)
    void accessToken_roundTrip_preservesClaims(
            @ForAll("userIds") Long userId,
            @ForAll("emails") String email,
            @ForAll("roles") Role role) {

        JwtTokenProvider provider = createProvider();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(Status.ACTIVE);

        String token = provider.generateAccessToken(user);

        assertThat(provider.validateToken(token)).isTrue();
        Claims claims = provider.getClaims(token);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo(role.name());
    }

    @Property(tries = 100)
    void refreshToken_roundTrip_preservesUserId(@ForAll("userIds") Long userId) {
        JwtTokenProvider provider = createProvider();

        User user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");
        user.setRole(Role.READER);
        user.setStatus(Status.ACTIVE);

        String token = provider.generateRefreshToken(user);

        assertThat(provider.validateToken(token)).isTrue();
        Claims claims = provider.getClaims(token);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
    }

    @Property(tries = 50)
    void invalidToken_neverValidates(@ForAll String randomString) {
        JwtTokenProvider provider = createProvider();
        assertThat(provider.validateToken(randomString)).isFalse();
    }

    @Provide
    Arbitrary<Long> userIds() {
        return Arbitraries.longs().between(1, 10000);
    }

    @Provide
    Arbitrary<String> emails() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20)
                .map(s -> s.toLowerCase() + "@example.com");
    }

    @Provide
    Arbitrary<Role> roles() {
        return Arbitraries.of(Role.ADMIN, Role.WRITER, Role.READER, Role.PENDING);
    }
}
