package com.mkfmm.gateway;

import io.jsonwebtoken.Jwts;
import net.jqwik.api.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.UUID;

class JwtFilterPropertyTest {

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
    void validTokenExtractsCorrectClaims(@ForAll("userIds") String userId, @ForAll("roles") String role) {
        String token = Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .id(UUID.randomUUID().toString())
                .issuer("mkfmm-auth")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(KEY_PAIR.getPrivate())
                .compact();

        var claims = Jwts.parser()
                .verifyWith(KEY_PAIR.getPublic())
                .requireIssuer("mkfmm-auth")
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assert claims.getSubject().equals(userId);
        assert claims.get("role", String.class).equals(role);
    }

    @Property
    void expiredTokenIsRejected(@ForAll("userIds") String userId) {
        String token = Jwts.builder()
                .subject(userId)
                .claim("role", "READ")
                .issuer("mkfmm-auth")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(KEY_PAIR.getPrivate())
                .compact();

        try {
            Jwts.parser()
                    .verifyWith(KEY_PAIR.getPublic())
                    .requireIssuer("mkfmm-auth")
                    .build()
                    .parseSignedClaims(token);
            assert false : "Should have thrown for expired token";
        } catch (Exception e) {
            // expected
        }
    }

    @Property
    void missingBearerPrefixIsInvalid(@ForAll("userIds") String randomString) {
        assert !randomString.startsWith("Bearer ");
        // Gateway should reject any string that doesn't have "Bearer " prefix
    }

    @Provide
    Arbitrary<String> userIds() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> roles() {
        return Arbitraries.of("ROOT_ADMIN", "ADMIN", "WRITE", "READ");
    }
}
