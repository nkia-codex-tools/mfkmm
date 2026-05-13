package com.mkfmm.user.application.service;

import com.mkfmm.user.application.port.outbound.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuditLogPurgeService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogPurgeService.class);

    private final AuditLogRepository auditLogRepository;
    private final int retentionDays;

    public AuditLogPurgeService(AuditLogRepository auditLogRepository,
                                @Value("${app.audit-log.retention-days}") int retentionDays) {
        this.auditLogRepository = auditLogRepository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void purgeExpiredAuditLogs() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deletedCount = auditLogRepository.deleteByPerformedAtBefore(cutoff);
        log.info("Purged {} audit logs older than {} days", deletedCount, retentionDays);
    }
}
