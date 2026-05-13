package com.mkfmm.user.domain.model;

public enum Role {
    ROOT_ADMIN,
    ADMIN,
    WRITE,
    READ;

    public boolean isAdmin() {
        return this == ROOT_ADMIN || this == ADMIN;
    }

    public boolean canManageUsers() {
        return isAdmin();
    }
}
