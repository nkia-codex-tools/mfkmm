package com.resourcemanager.history.controller;

import com.resourcemanager.history.dto.HistoryResponse;
import com.resourcemanager.history.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<Page<HistoryResponse>> getHistory(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long changedBy,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(historyService.getHistory(resourceType, changedBy, from, to, pageable));
    }
}
