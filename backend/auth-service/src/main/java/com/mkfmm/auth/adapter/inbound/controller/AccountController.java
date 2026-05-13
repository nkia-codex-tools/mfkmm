package com.mkfmm.auth.adapter.inbound.controller;

import com.mkfmm.auth.adapter.inbound.dto.UnlockRequest;
import com.mkfmm.auth.application.port.inbound.AccountManagementUseCase;
import com.mkfmm.auth.domain.model.LoginHistory;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AccountController {

    private final AccountManagementUseCase accountManagementUseCase;

    public AccountController(AccountManagementUseCase accountManagementUseCase) {
        this.accountManagementUseCase = accountManagementUseCase;
    }

    @PostMapping("/unlock")
    public ResponseEntity<Void> unlockAccount(@Valid @RequestBody UnlockRequest request,
                                              @RequestHeader("X-User-Id") String adminUserId) {
        accountManagementUseCase.unlockAccount(request.targetUserId(), adminUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/login-history")
    public ResponseEntity<Page<LoginHistory>> getLoginHistory(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) Boolean success,
            Pageable pageable) {
        Page<LoginHistory> history = accountManagementUseCase.getLoginHistory(userId, startDate, endDate, success, pageable);
        return ResponseEntity.ok(history);
    }
}
