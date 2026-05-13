package com.mkfmm.user.application.port.inbound;

import com.mkfmm.user.domain.model.AuditLog;
import com.mkfmm.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionUseCase {
    User grantPermission(String targetUserId, String newRole, String requesterId, String requesterRole);
    User revokePermission(String targetUserId, String requesterId, String requesterRole);
    Page<AuditLog> getAuditLogs(String action, String targetUserId, String performedBy,
                                 String startDate, String endDate, Pageable pageable, String requesterRole);
}
