package com.mkfmm.auth.application.port.inbound;

import com.mkfmm.auth.domain.model.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AccountManagementUseCase {
    void unlockAccount(String targetUserId, String adminUserId);
    Page<LoginHistory> getLoginHistory(String userId, Instant startDate, Instant endDate, Boolean success, Pageable pageable);
}
