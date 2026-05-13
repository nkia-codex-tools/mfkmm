package com.mkfmm.user.application.service;

import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import com.mkfmm.user.application.port.inbound.PermissionUseCase;
import com.mkfmm.user.application.port.outbound.AuditLogRepository;
import com.mkfmm.user.application.port.outbound.EventPublisherPort;
import com.mkfmm.user.application.port.outbound.UserRepository;
import com.mkfmm.user.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class PermissionService implements PermissionUseCase {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EventPublisherPort eventPublisher;

    public PermissionService(UserRepository userRepository,
                             AuditLogRepository auditLogRepository,
                             EventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public User grantPermission(String targetUserId, String newRole, String requesterId, String requesterRole) {
        if (!Role.valueOf(requesterRole).isAdmin()) {
            throw new ForbiddenException("Only admin can grant permissions");
        }

        Role role = Role.valueOf(newRole);
        if (role == Role.ROOT_ADMIN) {
            throw new BusinessException("ROOT_ADMIN cannot be assigned manually");
        }

        User user = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new BusinessException("User not found: " + targetUserId));

        if (!user.canChangeRole()) {
            throw new ForbiddenException("Root administrator role cannot be changed");
        }

        String previousRole = user.getRole().name();
        user.changeRole(role, requesterId);
        User saved = userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.PERMISSION_GRANTED, targetUserId, requesterId, previousRole, newRole));

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "PermissionChanged", Instant.now(), requesterId,
                        Map.of("targetUserId", targetUserId, "previousRole", previousRole, "newRole", newRole)),
                "user.permission.changed");

        return saved;
    }

    @Override
    public User revokePermission(String targetUserId, String requesterId, String requesterRole) {
        if (!Role.valueOf(requesterRole).isAdmin()) {
            throw new ForbiddenException("Only admin can revoke permissions");
        }

        User user = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new BusinessException("User not found: " + targetUserId));

        if (!user.canChangeRole()) {
            throw new ForbiddenException("Root administrator permission cannot be revoked");
        }

        if (user.getUserId().equals(requesterId)) {
            throw new BusinessException("Cannot revoke own admin permission");
        }

        String previousRole = user.getRole().name();
        user.changeRole(Role.READ, requesterId);
        User saved = userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.PERMISSION_REVOKED, targetUserId, requesterId, previousRole, "READ"));

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "PermissionChanged", Instant.now(), requesterId,
                        Map.of("targetUserId", targetUserId, "previousRole", previousRole, "newRole", "READ")),
                "user.permission.changed");

        return saved;
    }

    @Override
    public Page<AuditLog> getAuditLogs(String action, String targetUserId, String performedBy,
                                        String startDate, String endDate, Pageable pageable, String requesterRole) {
        if (!Role.valueOf(requesterRole).isAdmin()) {
            throw new ForbiddenException("Only admin can view audit logs");
        }

        Instant start = startDate != null ? Instant.parse(startDate) : null;
        Instant end = endDate != null ? Instant.parse(endDate) : null;

        return auditLogRepository.findAll(action, targetUserId, performedBy, start, end, pageable);
    }
}
