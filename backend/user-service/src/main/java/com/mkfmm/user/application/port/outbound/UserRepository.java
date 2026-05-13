package com.mkfmm.user.application.port.outbound;

import com.mkfmm.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
    Page<User> findAll(String userId, String name, String department, String role, Pageable pageable);
    void deleteById(String id);
}
