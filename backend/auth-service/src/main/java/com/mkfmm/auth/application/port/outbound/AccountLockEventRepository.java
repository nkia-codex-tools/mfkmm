package com.mkfmm.auth.application.port.outbound;

import com.mkfmm.auth.domain.model.AccountLockEvent;

import java.util.Optional;

public interface AccountLockEventRepository {
    AccountLockEvent save(AccountLockEvent event);
    Optional<AccountLockEvent> findLatestByUserId(String userId);
}
