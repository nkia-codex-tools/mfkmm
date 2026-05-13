package com.mkfmm.history.adapter.inbound.controller;

import com.mkfmm.history.application.service.WorkLogQueryService;
import com.mkfmm.history.domain.model.WorkLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/history")
public class WorkLogController {

    private final WorkLogQueryService queryService;

    public WorkLogController(WorkLogQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/work-logs/my")
    public ResponseEntity<Page<WorkLog>> getMy(
            @RequestParam(required = false) String workLogType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable,
            @RequestHeader("X-User-Id") String requesterId) {
        return ResponseEntity.ok(queryService.getMy(requesterId, workLogType, startDate, endDate, pageable));
    }

    @GetMapping("/work-logs")
    public ResponseEntity<Page<WorkLog>> getAll(
            @RequestParam(required = false) String workLogType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String resourceKey,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader("X-User-Role") String requesterRole) {
        return ResponseEntity.ok(queryService.getAll(workLogType, userId, resourceKey, startDate, endDate, pageable, requesterRole));
    }
}
