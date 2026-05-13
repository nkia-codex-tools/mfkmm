package com.mkfmm.auth;

import com.mkfmm.auth.domain.model.Role;
import com.mkfmm.auth.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import net.jqwik.api.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.UUID;

class JwtTokenPropertyTest {

    private static final KeyPair KEY_PAIR;

    static {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KEY_PAIR = gen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Property
    void encodeDecodeRoundTrip(@ForAll("userIds") String userId, @ForAll("roles") Role role) {
        String token = Jwts.builder()
                .subject(userId)
                .claim("role", role.name())
                .id(UUID.randomUUID().toString())
                .issuer("mkfmm-auth")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(KEY_PAIR.getPrivate())
                .compact();

        Claims claims = Jwts.parser()
                .verifyWith(KEY_PAIR.getPublic())
                .requireIssuer("mkfmm-auth")
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assert claims.getSubject().equals(userId);
        assert claims.get("role", String.class).equals(role.name());
    }

    @Property
    void tamperedTokenFails(@ForAll("userIds") String userId) {
        String token = Jwts.builder()
                .subject(userId)
                .claim("role", "ADMIN")
                .id(UUID.randomUUID().toString())
                .issuer("mkfmm-auth")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(KEY_PAIR.getPrivate())
                .compact();

        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        try {
            Jwts.parser()
                    .verifyWith(KEY_PAIR.getPublic())
                    .build()
                    .parseSignedClaims(tampered);
            assert false : "Should have thrown";
        } catch (Exception e) {
            // expected
        }
    }

    @Property
    void tokenTtlInvariant(@ForAll("ttlSeconds") int ttlSeconds) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + (long) ttlSeconds * 1000);

        String token = Jwts.builder()
                .subject("testuser")
                .claim("role", "READ")
                .issuer("mkfmm-auth")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(KEY_PAIR.getPrivate())
                .compact();

        Claims claims = Jwts.parser()
                .verifyWith(KEY_PAIR.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long actualTtl = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assert actualTtl == ttlSeconds;
    }

    @Provide
    Arbitrary<String> userIds() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
    }

    @Provide
    Arbitrary<Role> roles() {
        return Arbitraries.of(Role.values());
    }

    @Provide
    Arbitrary<Integer> ttlSeconds() {
        return Arbitraries.integers().between(60, 86400);
    }
}
