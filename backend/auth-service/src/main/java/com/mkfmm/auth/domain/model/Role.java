package com.mkfmm.auth.domain.model;

public enum Role {
    ROOT_ADMIN,
    ADMIN,
    WRITE,
    READ;

    public boolean isAdminOrAbove() {
        return this == ROOT_ADMIN || this == ADMIN;
    }
}
