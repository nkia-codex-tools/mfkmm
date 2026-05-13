package com.mkfmm.user;

import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import com.mkfmm.user.application.port.outbound.AuditLogRepository;
import com.mkfmm.user.application.port.outbound.EventPublisherPort;
import com.mkfmm.user.application.port.outbound.UserRepository;
import com.mkfmm.user.application.service.PermissionService;
import com.mkfmm.user.application.service.UserManagementService;
import com.mkfmm.user.domain.model.Role;
import com.mkfmm.user.domain.model.User;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserPermissionPropertyTest {

    @Property
    void rootAdmin_canNeverBeDeleted(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String adminId) {
        UserRepository userRepo = mock(UserRepository.class);
        AuditLogRepository auditRepo = mock(AuditLogRepository.class);
        EventPublisherPort eventPub = mock(EventPublisherPort.class);

        User rootAdmin = User.createRootAdmin("superadmin");
        rootAdmin.setId("root-id");
        when(userRepo.findById("root-id")).thenReturn(Optional.of(rootAdmin));

        UserManagementService service = new UserManagementService(userRepo, auditRepo, eventPub);

        assertThrows(ForbiddenException.class,
                () -> service.deleteUser("root-id", adminId, "ADMIN"));
        verify(userRepo, never()).deleteById(any());
    }

    @Property
    void rootAdmin_roleCanNeverBeChanged(
            @ForAll("assignableRoles") Role newRole,
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String adminId) {
        UserRepository userRepo = mock(UserRepository.class);
        AuditLogRepository auditRepo = mock(AuditLogRepository.class);
        EventPublisherPort eventPub = mock(EventPublisherPort.class);

        User rootAdmin = User.createRootAdmin("superadmin");
        when(userRepo.findByUserId("superadmin")).thenReturn(Optional.of(rootAdmin));

        PermissionService service = new PermissionService(userRepo, auditRepo, eventPub);

        assertThrows(ForbiddenException.class,
                () -> service.grantPermission("superadmin", newRole.name(), adminId, "ADMIN"));
    }

    @Property
    void admin_cannotRevokeOwnPermission(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String adminId) {
        UserRepository userRepo = mock(UserRepository.class);
        AuditLogRepository auditRepo = mock(AuditLogRepository.class);
        EventPublisherPort eventPub = mock(EventPublisherPort.class);

        User admin = new User(adminId, "Admin", null, null, Role.ADMIN, null, "system");
        when(userRepo.findByUserId(adminId)).thenReturn(Optional.of(admin));

        PermissionService service = new PermissionService(userRepo, auditRepo, eventPub);

        assertThrows(BusinessException.class,
                () -> service.revokePermission(adminId, adminId, "ADMIN"));
    }

    @Property
    void revokePermission_alwaysResultsInReadRole(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String targetId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String adminId) {
        Assume.that(!targetId.equals(adminId));

        UserRepository userRepo = mock(UserRepository.class);
        AuditLogRepository auditRepo = mock(AuditLogRepository.class);
        EventPublisherPort eventPub = mock(EventPublisherPort.class);

        User target = new User(targetId, "Target", null, null, Role.WRITE, null, "system");
        when(userRepo.findByUserId(targetId)).thenReturn(Optional.of(target));
        when(userRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(auditRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        PermissionService service = new PermissionService(userRepo, auditRepo, eventPub);
        User result = service.revokePermission(targetId, adminId, "ADMIN");

        assertEquals(Role.READ, result.getRole());
    }

    @Provide
    Arbitrary<Role> assignableRoles() {
        return Arbitraries.of(Role.READ, Role.WRITE, Role.ADMIN);
    }
}
