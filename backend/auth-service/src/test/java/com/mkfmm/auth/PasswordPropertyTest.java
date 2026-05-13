package com.mkfmm.auth;

import com.mkfmm.auth.domain.service.PasswordService;
import net.jqwik.api.*;

class PasswordPropertyTest {

    private final PasswordService passwordService = new PasswordService();

    @Property
    void hashVerifyRoundTrip(@ForAll("passwords") String password) {
        String hash = passwordService.hash(password);
        assert passwordService.matches(password, hash);
    }

    @Property
    void differentPasswordsProduceDifferentHashes(@ForAll("passwords") String pw1, @ForAll("passwords") String pw2) {
        Assume.that(!pw1.equals(pw2));
        String hash1 = passwordService.hash(pw1);
        assert !passwordService.matches(pw2, hash1);
    }

    @Property
    void hashIsNeverPlaintext(@ForAll("passwords") String password) {
        String hash = passwordService.hash(password);
        assert !hash.equals(password);
        assert hash.startsWith("$2a$") || hash.startsWith("$2b$");
    }

    @Provide
    Arbitrary<String> passwords() {
        return Arbitraries.strings().ofMinLength(4).ofMaxLength(50);
    }
}
