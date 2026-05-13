package com.mkfmm.history.domain.model;

public enum WorkLogType {
    SEARCH,
    RESOURCE_CREATED,
    RESOURCE_UPDATED,
    RESOURCE_DELETED,
    IMPORT,
    EXPORT,
    DEPLOY,
    USER_CREATED,
    USER_DELETED,
    PERMISSION_CHANGED,
    LOGIN,
    LOGOUT,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    SIMILARITY_CHOICE;

    public static WorkLogType fromEventType(String eventType) {
        return switch (eventType) {
            case "ResourceCreated" -> RESOURCE_CREATED;
            case "ResourceUpdated" -> RESOURCE_UPDATED;
            case "ResourceDeleted" -> RESOURCE_DELETED;
            case "ResourceSearched" -> SEARCH;
            case "ImportCompleted" -> IMPORT;
            case "ExportCompleted" -> EXPORT;
            case "DeployCompleted" -> DEPLOY;
            case "UserCreated" -> USER_CREATED;
            case "UserDeleted" -> USER_DELETED;
            case "PermissionChanged" -> PERMISSION_CHANGED;
            case "UserLoggedIn" -> LOGIN;
            case "UserLoggedOut" -> LOGOUT;
            case "AccountLocked" -> ACCOUNT_LOCKED;
            case "AccountUnlocked" -> ACCOUNT_UNLOCKED;
            case "SimilarityChoiceMade" -> SIMILARITY_CHOICE;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
