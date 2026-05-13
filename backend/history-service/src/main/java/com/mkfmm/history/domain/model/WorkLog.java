package com.mkfmm.history.domain.model;

import java.time.Instant;

public class WorkLog {
    private String id;
    private WorkLogType workLogType;
    private String userId;
    private String resourceId;
    private String resourceKey;
    private String previousValue;
    private String newValue;
    private String details;
    private boolean success;
    private String failureReason;
    private Instant performedAt;
    private String sourceEvent;
    private boolean markedForDeletion;

    public WorkLog(WorkLogType workLogType, String userId, Instant performedAt, String sourceEvent) {
        this.workLogType = workLogType;
        this.userId = userId;
        this.performedAt = performedAt;
        this.sourceEvent = sourceEvent;
        this.success = true;
        this.markedForDeletion = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public WorkLogType getWorkLogType() { return workLogType; }
    public String getUserId() { return userId; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getResourceKey() { return resourceKey; }
    public void setResourceKey(String resourceKey) { this.resourceKey = resourceKey; }
    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public boolean isSuccess() { return success; }
    public String getFailureReason() { return failureReason; }
    public Instant getPerformedAt() { return performedAt; }
    public String getSourceEvent() { return sourceEvent; }
    public boolean isMarkedForDeletion() { return markedForDeletion; }
    public void setMarkedForDeletion(boolean markedForDeletion) { this.markedForDeletion = markedForDeletion; }
}
