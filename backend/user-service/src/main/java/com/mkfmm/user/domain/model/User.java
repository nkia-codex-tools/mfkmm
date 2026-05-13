package com.mkfmm.user.domain.model;

import java.time.Instant;

public class User {
    private String id;
    private String userId;
    private String name;
    private String email;
    private String department;
    private Role role;
    private String memo;
    private boolean isRootAdmin;
    private boolean isLocked;
    private boolean mustChangePassword;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    public User(String userId, String name, String email, String department,
                Role role, String memo, String createdBy) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
        this.memo = memo;
        this.isRootAdmin = false;
        this.isLocked = false;
        this.mustChangePassword = true;
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
        this.updatedAt = Instant.now();
        this.updatedBy = createdBy;
    }

    public static User createRootAdmin(String userId) {
        User user = new User(userId, "Root Administrator", null, null, Role.ROOT_ADMIN, null, "SYSTEM");
        user.isRootAdmin = true;
        user.mustChangePassword = false;
        return user;
    }

    public boolean canBeDeleted() {
        return !isRootAdmin;
    }

    public boolean canChangeRole() {
        return !isRootAdmin;
    }

    public void updateInfo(String name, String email, String department, String memo, String updatedBy) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (department != null) this.department = department;
        if (memo != null) this.memo = memo;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void changeRole(Role newRole, String updatedBy) {
        this.role = newRole;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void unlock() {
        this.isLocked = false;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public Role getRole() { return role; }
    public String getMemo() { return memo; }
    public boolean isRootAdmin() { return isRootAdmin; }
    public boolean isLocked() { return isLocked; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
}
