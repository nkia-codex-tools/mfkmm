package com.mkfmm.user.adapter.inbound.controller;

import com.mkfmm.user.adapter.inbound.dto.GrantPermissionRequest;
import com.mkfmm.user.adapter.inbound.dto.UserResponse;
import com.mkfmm.user.application.port.inbound.PermissionUseCase;
import com.mkfmm.user.domain.model.AuditLog;
import com.mkfmm.user.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class PermissionController {

    private final PermissionUseCase permissionUseCase;

    public PermissionController(PermissionUseCase permissionUseCase) {
        this.permissionUseCase = permissionUseCase;
    }

    @PutMapping("/{userId}/permission")
    public ResponseEntity<UserResponse> grantPermission(@PathVariable String userId,
                                                         @Valid @RequestBody GrantPermissionRequest request,
                                                         @RequestHeader("X-User-Id") String requesterId,
                                                         @RequestHeader("X-User-Role") String requesterRole) {
        User user = permissionUseCase.grantPermission(userId, request.role(), requesterId, requesterRole);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{userId}/permission")
    public ResponseEntity<UserResponse> revokePermission(@PathVariable String userId,
                                                          @RequestHeader("X-User-Id") String requesterId,
                                                          @RequestHeader("X-User-Role") String requesterRole) {
        User user = permissionUseCase.revokePermission(userId, requesterId, requesterRole);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetUserId,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable,
            @RequestHeader("X-User-Role") String requesterRole) {
        Page<AuditLog> logs = permissionUseCase.getAuditLogs(action, targetUserId, performedBy, startDate, endDate, pageable, requesterRole);
        return ResponseEntity.ok(logs);
    }
}
