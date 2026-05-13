package com.mkfmm.shared.dto;

import java.time.Instant;

public record ErrorResponse(
    String code,
    String message,
    Instant timestamp,
    Object details
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Instant.now(), null);
    }

    public static ErrorResponse of(String code, String message, Object details) {
        return new ErrorResponse(code, message, Instant.now(), details);
    }
}
