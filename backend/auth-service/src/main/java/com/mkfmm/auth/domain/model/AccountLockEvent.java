package com.mkfmm.auth.domain.model;

import java.time.Instant;

public class AccountLockEvent {

    private String id;
    private String userId;
    private Instant lockedAt;
    private Instant unlockedAt;
    private String unlockedBy;

    public AccountLockEvent() {}

    public static AccountLockEvent locked(String userId) {
        AccountLockEvent e = new AccountLockEvent();
        e.userId = userId;
        e.lockedAt = Instant.now();
        return e;
    }

    public void unlock(String adminUserId) {
        this.unlockedAt = Instant.now();
        this.unlockedBy = adminUserId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getLockedAt() { return lockedAt; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }
    public Instant getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Instant unlockedAt) { this.unlockedAt = unlockedAt; }
    public String getUnlockedBy() { return unlockedBy; }
    public void setUnlockedBy(String unlockedBy) { this.unlockedBy = unlockedBy; }
}
