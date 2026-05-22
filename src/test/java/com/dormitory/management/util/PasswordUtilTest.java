package com.dormitory.management.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void hashAndMatchShouldWork() {
        String raw = "StrongPass123";
        String hashed = PasswordUtil.hashPassword(raw);

        Assertions.assertTrue(hashed.startsWith("pbkdf2$"));
        Assertions.assertTrue(PasswordUtil.matches(raw, hashed));
        Assertions.assertFalse(PasswordUtil.matches("WrongPass", hashed));
    }

    @Test
    void legacyPlaintextShouldStillMatchForMigrationPath() {
        Assertions.assertTrue(PasswordUtil.matches("admin123", "admin123"));
        Assertions.assertFalse(PasswordUtil.matches("admin124", "admin123"));
    }
}
