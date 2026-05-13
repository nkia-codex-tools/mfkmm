package com.mkfmm.user;

import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import com.mkfmm.user.application.port.outbound.AuditLogRepository;
import com.mkfmm.user.application.port.outbound.EventPublisherPort;
import com.mkfmm.user.application.port.outbound.UserRepository;
import com.mkfmm.user.application.service.UserManagementService;
import com.mkfmm.user.domain.model.Role;
import com.mkfmm.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserManagementServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private EventPublisherPort eventPublisher;

    private UserManagementService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserManagementService(userRepository, auditLogRepository, eventPublisher);
    }

    @Test
    void createUser_success() {
        when(userRepository.existsByUserId("newuser")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = service.createUser("newuser", "New User", "new@test.com",
                "Dev", "WRITE", "pass123", null, "admin1");

        assertNotNull(result);
        assertEquals("newuser", result.getUserId());
        assertEquals(Role.WRITE, result.getRole());
        assertTrue(result.isMustChangePassword());
        verify(eventPublisher).publish(any(), eq("user.created"));
    }

    @Test
    void createUser_duplicateId_throws() {
        when(userRepository.existsByUserId("existing")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.createUser("existing", "Name", null, null, "WRITE", "pass", null, "admin1"));
    }

    @Test
    void createUser_rootAdminRole_throws() {
        assertThrows(BusinessException.class,
                () -> service.createUser("user1", "Name", null, null, "ROOT_ADMIN", "pass", null, "admin1"));
    }

    @Test
    void deleteUser_rootAdmin_throws() {
        User rootAdmin = User.createRootAdmin("superadmin");
        rootAdmin.setId("id1");
        when(userRepository.findById("id1")).thenReturn(Optional.of(rootAdmin));

        assertThrows(ForbiddenException.class,
                () -> service.deleteUser("id1", "admin1", "ADMIN"));
    }

    @Test
    void deleteUser_success() {
        User user = new User("user1", "Name", null, null, Role.WRITE, null, "admin1");
        user.setId("id1");
        when(userRepository.findById("id1")).thenReturn(Optional.of(user));

        service.deleteUser("id1", "admin1", "ADMIN");

        verify(userRepository).deleteById("id1");
        verify(eventPublisher).publish(any(), eq("user.deleted"));
    }

    @Test
    void unlockAccount_notLocked_idempotent() {
        User user = new User("user1", "Name", null, null, Role.WRITE, null, "admin1");
        when(userRepository.findByUserId("user1")).thenReturn(Optional.of(user));

        service.unlockAccount("user1", "admin1", "ADMIN");

        verify(userRepository, never()).save(any());
    }
}
