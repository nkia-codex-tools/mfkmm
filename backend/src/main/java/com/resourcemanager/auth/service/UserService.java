package com.resourcemanager.auth.service;

import com.resourcemanager.auth.dto.RoleUpdateRequest;
import com.resourcemanager.auth.dto.UserResponse;
import com.resourcemanager.auth.entity.Role;
import com.resourcemanager.auth.entity.Status;
import com.resourcemanager.auth.entity.User;
import com.resourcemanager.auth.repository.UserRepository;
import com.resourcemanager.common.exception.BadRequestException;
import com.resourcemanager.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers() {
        return userRepository.findByRole(Role.PENDING).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateUserRole(Long targetUserId, Long currentUserId, RoleUpdateRequest request) {
        if (targetUserId.equals(currentUserId)) {
            throw new BadRequestException("Cannot modify your own role");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Role newRole;
        try {
            newRole = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.role());
        }

        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot remove the last admin");
            }
        }

        Role oldRole = target.getRole();
        target.setRole(newRole);

        if (target.getStatus() == Status.PENDING && newRole != Role.PENDING) {
            target.setStatus(Status.ACTIVE);
        }

        userRepository.save(target);
        log.info("Role updated: targetUserId={}, oldRole={}, newRole={}, changedBy={}",
                targetUserId, oldRole, newRole, currentUserId);

        return UserResponse.from(target);
    }
}
