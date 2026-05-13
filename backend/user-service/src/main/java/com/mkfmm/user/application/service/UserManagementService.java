package com.mkfmm.user.application.service;

import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import com.mkfmm.user.application.port.inbound.UserManagementUseCase;
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
public class UserManagementService implements UserManagementUseCase {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EventPublisherPort eventPublisher;

    public UserManagementService(UserRepository userRepository,
                                  AuditLogRepository auditLogRepository,
                                  EventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public User createUser(String userId, String name, String email, String department,
                           String role, String password, String memo, String requesterId) {
        Role newRole = Role.valueOf(role);
        if (newRole == Role.ROOT_ADMIN) {
            throw new BusinessException("ROOT_ADMIN cannot be assigned manually");
        }
        if (userRepository.existsByUserId(userId)) {
            throw new BusinessException("User ID already exists: " + userId);
        }

        User user = new User(userId, name, email, department, newRole, memo, requesterId);
        User saved = userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.USER_CREATED, userId, requesterId, null, role));

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "UserCreated", Instant.now(), requesterId,
                        Map.of("userId", userId, "name", name, "role", role)),
                "user.created");

        return saved;
    }

    @Override
    public User updateUser(String id, Map<String, String> updates, String requesterId, String requesterRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found: " + id));

        if (user.isRootAdmin() && !user.getUserId().equals(requesterId)) {
            throw new ForbiddenException("Root administrator can only be updated by themselves");
        }

        boolean isSelf = user.getUserId().equals(requesterId);
        if (!isSelf && !Role.valueOf(requesterRole).isAdmin()) {
            throw new ForbiddenException("Only admin can update other users");
        }

        String previousValue = String.format("{name:%s, email:%s, department:%s}", user.getName(), user.getEmail(), user.getDepartment());
        user.updateInfo(updates.get("name"), updates.get("email"), updates.get("department"), updates.get("memo"), requesterId);
        User saved = userRepository.save(user);

        String newValueStr = String.format("{name:%s, email:%s, department:%s}", saved.getName(), saved.getEmail(), saved.getDepartment());
        auditLogRepository.save(new AuditLog(AuditAction.USER_UPDATED, user.getUserId(), requesterId, previousValue, newValueStr));

        return saved;
    }

    @Override
    public void deleteUser(String id, String requesterId, String requesterRole) {
        if (!Role.valueOf(requesterRole).isAdmin()) {
            throw new ForbiddenException("Only admin can delete users");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found: " + id));

        if (!user.canBeDeleted()) {
            throw new ForbiddenException("Root administrator cannot be deleted");
        }

        userRepository.deleteById(id);

        auditLogRepository.save(new AuditLog(AuditAction.USER_DELETED, user.getUserId(), requesterId, null, null));

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "UserDeleted", Instant.now(), requesterId,
                        Map.of("userId", user.getUserId(), "deletedBy", requesterId)),
                "user.deleted");
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found: " + id));
    }

    @Override
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId));
    }

    @Override
    public Page<User> getUsers(String userId, String name, String department, String role,
                               Pageable pageable, String requesterRole) {
        if (!Role.valueOf(requesterRole).isAdmin()) {
            throw new ForbiddenException("Only admin can list all users");
        }
        return userRepository.findAll(userId, name, department, role, pageable);
    }

    @Override
    public void unlockAccount(String targetUserId, String requesterId, String requesterRole) {
        if (!Role.valueOf(requesterRole).isAdmin()) {
            throw new ForbiddenException("Only admin can unlock accounts");
        }

        User user = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new BusinessException("User not found: " + targetUserId));

        if (!user.isLocked()) {
            return;
        }

        user.unlock();
        userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.ACCOUNT_UNLOCKED, targetUserId, requesterId, "locked", "unlocked"));

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "AccountUnlocked", Instant.now(), requesterId,
                        Map.of("userId", targetUserId, "unlockedBy", requesterId)),
                "user.account.unlocked");
    }
}
