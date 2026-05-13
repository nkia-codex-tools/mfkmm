package com.mkfmm.user.adapter.inbound.controller;

import com.mkfmm.user.adapter.inbound.dto.*;
import com.mkfmm.user.application.port.inbound.UserManagementUseCase;
import com.mkfmm.user.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementUseCase userManagementUseCase;

    public UserController(UserManagementUseCase userManagementUseCase) {
        this.userManagementUseCase = userManagementUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                    @RequestHeader("X-User-Id") String requesterId,
                                                    @RequestHeader("X-User-Role") String requesterRole) {
        User user = userManagementUseCase.createUser(
                request.userId(), request.name(), request.email(), request.department(),
                request.role(), request.password(), request.memo(), requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                    @Valid @RequestBody UpdateUserRequest request,
                                                    @RequestHeader("X-User-Id") String requesterId,
                                                    @RequestHeader("X-User-Role") String requesterRole) {
        Map<String, String> updates = new HashMap<>();
        if (request.name() != null) updates.put("name", request.name());
        if (request.email() != null) updates.put("email", request.email());
        if (request.department() != null) updates.put("department", request.department());
        if (request.memo() != null) updates.put("memo", request.memo());

        User user = userManagementUseCase.updateUser(id, updates, requesterId, requesterRole);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id,
                                           @RequestHeader("X-User-Id") String requesterId,
                                           @RequestHeader("X-User-Role") String requesterRole) {
        userManagementUseCase.deleteUser(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        User user = userManagementUseCase.getUserById(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        User user = userManagementUseCase.getUserByUserId(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            Pageable pageable,
            @RequestHeader("X-User-Role") String requesterRole) {
        Page<User> users = userManagementUseCase.getUsers(userId, name, department, role, pageable, requesterRole);
        return ResponseEntity.ok(users.map(UserResponse::from));
    }

    @PostMapping("/{userId}/unlock")
    public ResponseEntity<Void> unlockAccount(@PathVariable String userId,
                                              @RequestHeader("X-User-Id") String requesterId,
                                              @RequestHeader("X-User-Role") String requesterRole) {
        userManagementUseCase.unlockAccount(userId, requesterId, requesterRole);
        return ResponseEntity.ok().build();
    }
}
