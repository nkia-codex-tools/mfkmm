package com.mkfmm.auth.domain.model;

import java.time.Instant;

public class User {

    private String id;
    private String userId;
    private String passwordHash;
    private Role role;
    private boolean isLocked;
    private int failedLoginAttempts;
    private boolean isRootAdmin;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {}

    public User(String userId, String passwordHash, Role role, boolean isRootAdmin) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isRootAdmin = isRootAdmin;
        this.isLocked = false;
        this.failedLoginAttempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        this.updatedAt = Instant.now();
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.updatedAt = Instant.now();
    }

    public void lock() {
        this.isLocked = true;
        this.updatedAt = Instant.now();
    }

    public void unlock() {
        this.isLocked = false;
        this.updatedAt = Instant.now();
    }

    public boolean shouldLock() {
        return this.failedLoginAttempts >= 3;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public boolean isRootAdmin() { return isRootAdmin; }
    public void setRootAdmin(boolean rootAdmin) { isRootAdmin = rootAdmin; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
