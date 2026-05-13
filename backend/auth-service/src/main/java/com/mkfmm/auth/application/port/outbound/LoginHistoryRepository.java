package com.mkfmm.auth.application.port.outbound;

import com.mkfmm.auth.domain.model.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface LoginHistoryRepository {
    LoginHistory save(LoginHistory history);
    Page<LoginHistory> findByFilters(String userId, Instant startDate, Instant endDate, Boolean success, Pageable pageable);
}
