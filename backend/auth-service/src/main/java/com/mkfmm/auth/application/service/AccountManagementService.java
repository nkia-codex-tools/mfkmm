package com.mkfmm.auth.application.service;

import com.mkfmm.auth.application.port.inbound.AccountManagementUseCase;
import com.mkfmm.auth.application.port.outbound.*;
import com.mkfmm.auth.domain.model.AccountLockEvent;
import com.mkfmm.auth.domain.model.LoginHistory;
import com.mkfmm.auth.domain.model.User;
import com.mkfmm.shared.event.AuthEvents;
import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountManagementService implements AccountManagementUseCase {

    private final UserRepository userRepository;
    private final AccountLockEventRepository accountLockEventRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final EventPublisherPort eventPublisher;

    public AccountManagementService(
            UserRepository userRepository,
            AccountLockEventRepository accountLockEventRepository,
            LoginHistoryRepository loginHistoryRepository,
            EventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.accountLockEventRepository = accountLockEventRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void unlockAccount(String targetUserId, String adminUserId) {
        User admin = userRepository.findByUserId(adminUserId)
                .orElseThrow(() -> new BusinessException("COMMON_002", "Admin not found"));

        if (!admin.getRole().isAdminOrAbove()) {
            throw new ForbiddenException("Insufficient permissions");
        }

        User target = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new BusinessException("COMMON_002", "Target user not found"));

        if (!target.isLocked()) {
            return;
        }

        target.unlock();
        userRepository.save(target);

        accountLockEventRepository.findLatestByUserId(targetUserId)
                .ifPresent(event -> {
                    event.unlock(adminUserId);
                    accountLockEventRepository.save(event);
                });

        eventPublisher.publish(BaseEvent.of(
                AuthEvents.ACCOUNT_UNLOCKED, targetUserId,
                new AuthEvents.AccountUnlockedPayload(targetUserId, Instant.now(), adminUserId)
        ));
    }

    @Override
    public Page<LoginHistory> getLoginHistory(String userId, Instant startDate, Instant endDate, Boolean success, Pageable pageable) {
        return loginHistoryRepository.findByFilters(userId, startDate, endDate, success, pageable);
    }
}
