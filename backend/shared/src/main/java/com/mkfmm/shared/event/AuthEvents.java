package com.mkfmm.shared.event;

import java.time.Instant;

public final class AuthEvents {

    private AuthEvents() {}

    public static final String USER_LOGGED_IN = "auth.user.logged-in";
    public static final String USER_LOGGED_OUT = "auth.user.logged-out";
    public static final String ACCOUNT_LOCKED = "auth.account.locked";
    public static final String ACCOUNT_UNLOCKED = "auth.account.unlocked";
    public static final String PASSWORD_CHANGED = "auth.password.changed";

    public record UserLoggedInPayload(String userId, String ipAddress, Instant loginAt) {}
    public record UserLoggedOutPayload(String userId, Instant logoutAt) {}
    public record AccountLockedPayload(String userId, Instant lockedAt, int failedAttempts) {}
    public record AccountUnlockedPayload(String userId, Instant unlockedAt, String unlockedBy) {}
    public record PasswordChangedPayload(String userId, String changedBy, Instant changedAt) {}
}
