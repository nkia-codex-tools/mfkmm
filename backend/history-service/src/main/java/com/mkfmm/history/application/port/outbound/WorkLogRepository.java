package com.mkfmm.history.application.port.outbound;

import com.mkfmm.history.domain.model.WorkLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface WorkLogRepository {
    WorkLog save(WorkLog workLog);
    boolean existsBySourceEvent(String sourceEvent);
    Page<WorkLog> findAll(String workLogType, String userId, String resourceKey,
                          Instant startDate, Instant endDate, Pageable pageable);
    Page<WorkLog> findByUserId(String userId, String workLogType,
                               Instant startDate, Instant endDate, Pageable pageable);
    long markForDeletionByUserId(String userId);
    long deleteMarkedForDeletion();
    long deleteSearchLogsOlderThan(Instant cutoff);
}
