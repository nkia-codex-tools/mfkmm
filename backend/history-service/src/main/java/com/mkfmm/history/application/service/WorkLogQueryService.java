package com.mkfmm.history.application.service;

import com.mkfmm.history.application.port.outbound.WorkLogRepository;
import com.mkfmm.history.domain.model.WorkLog;
import com.mkfmm.shared.exception.ForbiddenException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class WorkLogQueryService {

    private final WorkLogRepository workLogRepository;

    public WorkLogQueryService(WorkLogRepository workLogRepository) {
        this.workLogRepository = workLogRepository;
    }

    public Page<WorkLog> getAll(String workLogType, String userId, String resourceKey,
                                String startDate, String endDate, Pageable pageable, String requesterRole) {
        if (!"ADMIN".equals(requesterRole) && !"ROOT_ADMIN".equals(requesterRole)) {
            throw new ForbiddenException("Only admin can view all work logs");
        }
        Instant start = startDate != null ? Instant.parse(startDate) : null;
        Instant end = endDate != null ? Instant.parse(endDate) : null;
        return workLogRepository.findAll(workLogType, userId, resourceKey, start, end, pageable);
    }

    public Page<WorkLog> getMy(String requesterId, String workLogType,
                               String startDate, String endDate, Pageable pageable) {
        Instant start = startDate != null ? Instant.parse(startDate) : null;
        Instant end = endDate != null ? Instant.parse(endDate) : null;
        return workLogRepository.findByUserId(requesterId, workLogType, start, end, pageable);
    }
}
