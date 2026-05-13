package com.mkfmm.user.domain.model;

import java.time.Instant;

public class AuditLog {
    private String id;
    private AuditAction action;
    private String targetUserId;
    private String performedBy;
    private String previousValue;
    private String newValue;
    private String reason;
    private Instant performedAt;

    public AuditLog(AuditAction action, String targetUserId, String performedBy,
                    String previousValue, String newValue) {
        this.action = action;
        this.targetUserId = targetUserId;
        this.performedBy = performedBy;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.performedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public AuditAction getAction() { return action; }
    public String getTargetUserId() { return targetUserId; }
    public String getPerformedBy() { return performedBy; }
    public String getPreviousValue() { return previousValue; }
    public String getNewValue() { return newValue; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getPerformedAt() { return performedAt; }
}
