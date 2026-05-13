package com.mkfmm.auth.domain.model;

import java.time.Instant;

public class LoginHistory {

    private String id;
    private String userId;
    private Instant loginAt;
    private String ipAddress;
    private boolean success;
    private String failureReason;

    public LoginHistory() {}

    public static LoginHistory success(String userId, String ipAddress) {
        LoginHistory h = new LoginHistory();
        h.userId = userId;
        h.loginAt = Instant.now();
        h.ipAddress = ipAddress;
        h.success = true;
        return h;
    }

    public static LoginHistory failure(String userId, String ipAddress, String reason) {
        LoginHistory h = new LoginHistory();
        h.userId = userId;
        h.loginAt = Instant.now();
        h.ipAddress = ipAddress;
        h.success = false;
        h.failureReason = reason;
        return h;
    }

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
