package com.mkfmm.user.application.port.inbound;

import com.mkfmm.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface UserManagementUseCase {
    User createUser(String userId, String name, String email, String department,
                    String role, String password, String memo, String requesterId);
    User updateUser(String id, Map<String, String> updates, String requesterId, String requesterRole);
    void deleteUser(String id, String requesterId, String requesterRole);
    User getUserById(String id);
    User getUserByUserId(String userId);
    Page<User> getUsers(String userId, String name, String department, String role, Pageable pageable, String requesterRole);
    void unlockAccount(String targetUserId, String requesterId, String requesterRole);
}
