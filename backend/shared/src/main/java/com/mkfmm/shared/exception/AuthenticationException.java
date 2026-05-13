package com.mkfmm.shared.exception;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(String code, String message) {
        super(code, message);
    }
}
