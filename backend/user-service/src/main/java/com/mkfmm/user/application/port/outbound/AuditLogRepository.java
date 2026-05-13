package com.mkfmm.user.application.port.outbound;

import com.mkfmm.user.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);
    Page<AuditLog> findAll(String action, String targetUserId, String performedBy,
                           Instant startDate, Instant endDate, Pageable pageable);
    long deleteByPerformedAtBefore(Instant cutoff);
}
