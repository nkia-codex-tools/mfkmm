package com.mkfmm.shared.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super("AUTH_005", message);
    }
}
