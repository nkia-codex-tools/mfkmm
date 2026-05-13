package com.mkfmm.auth.adapter.outbound.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "account_lock_events")
public class AccountLockEventDocument {

    @Id
    private String id;
    @Indexed
    private String userId;
    private Instant lockedAt;
    private Instant unlockedAt;
    private String unlockedBy;

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
