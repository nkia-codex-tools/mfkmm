package com.resourcemanager.history.dto;

import com.resourcemanager.history.entity.ChangeHistory;

import java.time.Instant;

public record HistoryResponse(
        Long id,
        String resourceType,
        Long resourceId,
        String changeType,
        String fieldName,
        String oldValue,
        String newValue,
        Long changedBy,
        Instant changedAt
) {
    public static HistoryResponse from(ChangeHistory entity) {
        return new HistoryResponse(
                entity.getId(), entity.getResourceType(), entity.getResourceId(),
                entity.getChangeType(), entity.getFieldName(),
                entity.getOldValue(), entity.getNewValue(),
                entity.getChangedBy(), entity.getChangedAt()
        );
    }
}
