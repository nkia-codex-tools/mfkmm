package com.resourcemanager.auth.service;

import com.resourcemanager.auth.dto.RoleUpdateRequest;
import com.resourcemanager.auth.entity.Role;
import com.resourcemanager.auth.entity.Status;
import com.resourcemanager.auth.entity.User;
import com.resourcemanager.auth.repository.UserRepository;
import com.resourcemanager.common.exception.BadRequestException;
import com.resourcemanager.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private User targetUser;

    @BeforeEach
    void setUp() {
        targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("user@example.com");
        targetUser.setName("User");
        targetUser.setRole(Role.PENDING);
        targetUser.setStatus(Status.PENDING);
    }

    @Test
    void updateRole_pendingToWriter_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        var response = userService.updateUserRole(2L, 1L, new RoleUpdateRequest("WRITER"));

        assertThat(response.role()).isEqualTo("WRITER");
        assertThat(targetUser.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void updateRole_selfModification_throwsBadRequest() {
        assertThatThrownBy(() -> userService.updateUserRole(1L, 1L, new RoleUpdateRequest("READER")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own role");
    }

    @Test
    void updateRole_lastAdmin_throwsBadRequest() {
        targetUser.setRole(Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.updateUserRole(2L, 1L, new RoleUpdateRequest("WRITER")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("last admin");
    }

    @Test
    void updateRole_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(99L, 1L, new RoleUpdateRequest("WRITER")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateRole_invalidRole_throwsBadRequest() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        assertThatThrownBy(() -> userService.updateUserRole(2L, 1L, new RoleUpdateRequest("INVALID")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid role");
    }
}
