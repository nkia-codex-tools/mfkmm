package com.mkfmm.auth.application.port.outbound;

import com.mkfmm.auth.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUserId(String userId);
    User save(User user);
    boolean existsByRole(com.mkfmm.auth.domain.model.Role role);
}
