package com.mkfmm.history.application.service;

import com.mkfmm.history.application.port.outbound.WorkLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class RetentionService {

    private static final Logger log = LoggerFactory.getLogger(RetentionService.class);

    private final WorkLogRepository workLogRepository;
    private final int searchRetentionDays;

    public RetentionService(WorkLogRepository workLogRepository,
                            @Value("${app.retention.search-days}") int searchRetentionDays) {
        this.workLogRepository = workLogRepository;
        this.searchRetentionDays = searchRetentionDays;
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void purgeExpiredSearchLogs() {
        Instant cutoff = Instant.now().minus(searchRetentionDays, ChronoUnit.DAYS);
        long deleted = workLogRepository.deleteSearchLogsOlderThan(cutoff);
        log.info("Purged {} search logs older than {} days", deleted, searchRetentionDays);
    }

    @Scheduled(cron = "0 30 4 * * *")
    public void purgeMarkedForDeletion() {
        long deleted = workLogRepository.deleteMarkedForDeletion();
        log.info("Purged {} work logs marked for deletion", deleted);
    }
}
