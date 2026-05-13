package com.mkfmm.resource.application.service;

import com.mkfmm.resource.application.port.outbound.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class HardDeleteService {

    private static final Logger log = LoggerFactory.getLogger(HardDeleteService.class);

    private final ResourceRepository resourceRepository;
    private final int retentionDays;

    public HardDeleteService(ResourceRepository resourceRepository,
                             @Value("${app.hard-delete.retention-days}") int retentionDays) {
        this.resourceRepository = resourceRepository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void hardDeleteExpiredResources() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deletedCount = resourceRepository.hardDeleteByDeletedAtBefore(cutoff);
        log.info("Hard deleted {} resources older than {} days", deletedCount, retentionDays);
    }
}
