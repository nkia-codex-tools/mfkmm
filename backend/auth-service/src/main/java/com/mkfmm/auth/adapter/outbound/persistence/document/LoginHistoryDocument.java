package com.mkfmm.auth.adapter.outbound.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "login_history")
@CompoundIndex(name = "userId_loginAt", def = "{'userId': 1, 'loginAt': -1}")
public class LoginHistoryDocument {

    @Id
    private String id;
    private String userId;
    private Instant loginAt;
    private String ipAddress;
    private boolean success;
    private String failureReason;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getLoginAt() { return loginAt; }
    public void setLoginAt(Instant loginAt) { this.loginAt = loginAt; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
