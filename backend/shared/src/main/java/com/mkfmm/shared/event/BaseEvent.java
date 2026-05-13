package com.mkfmm.shared.event;

import java.time.Instant;

public record BaseEvent(
    String eventId,
    String eventType,
    Instant timestamp,
    String userId,
    Object payload
) {
    public static BaseEvent of(String eventType, String userId, Object payload) {
        return new BaseEvent(
            java.util.UUID.randomUUID().toString(),
            eventType,
            Instant.now(),
            userId,
            payload
        );
    }
}
