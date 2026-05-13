package com.mkfmm.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private String id;
    private String userId;
    private String token;
    private String jti;
    private Instant expiresAt;
    private Instant createdAt;
    private boolean revoked;

    public RefreshToken() {}

    public RefreshToken(String userId, long ttlSeconds) {
        this.userId = userId;
        this.token = UUID.randomUUID().toString();
        this.jti = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plusSeconds(ttlSeconds);
        this.revoked = false;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
