package com.resourcemanager.auth.controller;

import com.resourcemanager.auth.dto.RoleUpdateRequest;
import com.resourcemanager.auth.dto.UserResponse;
import com.resourcemanager.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserResponse>> getPendingUsers() {
        return ResponseEntity.ok(userService.getPendingUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id,
                                                       @Valid @RequestBody RoleUpdateRequest request,
                                                       Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        UserResponse response = userService.updateUserRole(id, currentUserId, request);
        return ResponseEntity.ok(response);
    }
}
