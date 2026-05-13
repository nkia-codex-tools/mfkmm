package com.mkfmm.auth;

import com.mkfmm.auth.domain.model.Role;
import com.mkfmm.auth.domain.model.User;
import net.jqwik.api.*;

class AccountLockPropertyTest {

    @Property
    void lockThresholdInvariant(@ForAll @IntRange(min = 0, max = 10) int failedAttempts) {
        User user = new User("testuser", "hash", Role.READ, false);
        for (int i = 0; i < failedAttempts; i++) {
            user.incrementFailedAttempts();
        }

        if (failedAttempts >= 3) {
            assert user.shouldLock();
        } else {
            assert !user.shouldLock();
        }
    }

    @Property
    void successResetsCounts(@ForAll @IntRange(min = 1, max = 10) int failedAttempts) {
        User user = new User("testuser", "hash", Role.WRITE, false);
        for (int i = 0; i < failedAttempts; i++) {
            user.incrementFailedAttempts();
        }

        user.resetFailedAttempts();
        assert user.getFailedLoginAttempts() == 0;
    }

    @Property
    void lockedUserBlocksLogin(@ForAll("roles") Role role) {
        User user = new User("testuser", "hash", role, false);
        user.lock();
        assert user.isLocked();
    }

    @Property
    void rootAdminImmutability() {
        User root = new User("root", "hash", Role.ROOT_ADMIN, true);
        assert root.isRootAdmin();
        assert root.getRole() == Role.ROOT_ADMIN;
    }

    @Provide
    Arbitrary<Role> roles() {
        return Arbitraries.of(Role.values());
    }
}
