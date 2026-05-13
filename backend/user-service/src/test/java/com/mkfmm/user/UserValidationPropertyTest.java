package com.mkfmm.user;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.constraints.StringLength;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationPropertyTest {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    @Property
    void validUserIds_alwaysMatchPattern(
            @ForAll("validUserIds") String userId) {
        assertTrue(USER_ID_PATTERN.matcher(userId).matches(),
                "Valid userId should match pattern: " + userId);
    }

    @Property
    void invalidUserIds_neverMatchPattern(
            @ForAll("invalidUserIds") String userId) {
        assertFalse(USER_ID_PATTERN.matcher(userId).matches(),
                "Invalid userId should not match pattern: " + userId);
    }

    @Property
    void userIdPattern_isAlphanumericOnly(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String alpha,
            @ForAll @NumericChars @StringLength(min = 0, max = 5) String numeric) {
        String combined = alpha + numeric;
        assertTrue(USER_ID_PATTERN.matcher(combined).matches());
    }

    @Provide
    Arbitrary<String> validUserIds() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> invalidUserIds() {
        return Arbitraries.of(
                "user@name", "user name", "user.name", "user-name",
                "user!123", "사용자", "user#1", "");
    }
}
