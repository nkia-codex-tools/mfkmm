package com.mkfmm.history.adapter.outbound.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "work_logs")
public class WorkLogDocument {
    @Id
    private String id;
    private String workLogType;
    private String userId;
    private String resourceId;
    private String resourceKey;
    private String previousValue;
    private String newValue;
    private String details;
    private boolean success;
    private String failureReason;
    private Instant performedAt;
    @Indexed(unique = true)
    private String sourceEvent;
    private boolean markedForDeletion;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWorkLogType() { return workLogType; }
    public void setWorkLogType(String workLogType) { this.workLogType = workLogType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
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
    public void setSuccess(boolean success) { this.success = success; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getPerformedAt() { return performedAt; }
    public void setPerformedAt(Instant performedAt) { this.performedAt = performedAt; }
    public String getSourceEvent() { return sourceEvent; }
    public void setSourceEvent(String sourceEvent) { this.sourceEvent = sourceEvent; }
    public boolean isMarkedForDeletion() { return markedForDeletion; }
    public void setMarkedForDeletion(boolean markedForDeletion) { this.markedForDeletion = markedForDeletion; }
}
