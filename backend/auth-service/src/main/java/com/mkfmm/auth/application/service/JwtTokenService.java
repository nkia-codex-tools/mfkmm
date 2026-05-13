package com.mkfmm.auth.application.service;

import com.mkfmm.auth.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenTtl;
    private final long refreshTokenTtl;
    private final String issuer;

    public JwtTokenService(
            @Value("${jwt.private-key-path}") String privateKeyPath,
            @Value("${jwt.public-key-path}") String publicKeyPath,
            @Value("${jwt.access-token-ttl}") long accessTokenTtl,
            @Value("${jwt.refresh-token-ttl}") long refreshTokenTtl,
            @Value("${jwt.issuer}") String issuer) throws Exception {
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
        this.issuer = issuer;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenTtl * 1000);

        return Jwts.builder()
                .subject(user.getUserId())
                .claim("role", user.getRole().name())
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public long getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String keyContent = readKeyFile(path);
        keyContent = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        String keyContent = readKeyFile(path);
        keyContent = keyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String readKeyFile(String path) throws Exception {
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            try (var is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) throw new IllegalStateException("Key file not found: " + path);
                return new String(is.readAllBytes());
            }
        }
        return java.nio.file.Files.readString(java.nio.file.Path.of(path));
    }
}
