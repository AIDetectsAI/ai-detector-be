package org.example.aidetectorbe.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = PasswordHasher.class)
class PasswordHasherTest {

    @Autowired
    private PasswordHasher passwordHasher;

    @Test
    void hashPassword_shouldReturnHashedString() {
        String password = "testPassword123";
        String hashed = passwordHasher.hashPassword(password);

        assertNotNull(hashed, "Hashed password should not be null");
        assertNotEquals(password, hashed, "Hashed password should not equal the original password");
    }

    @Test
    void hashPassword_shouldReturnDifferentHashesForSamePassword() {
        String password = "testPassword123";
        String hashed1 = passwordHasher.hashPassword(password);
        String hashed2 = passwordHasher.hashPassword(password);

        assertNotNull(hashed1);
        assertNotNull(hashed2);
        assertNotEquals(hashed1, hashed2, "Hashing the same password should produce different hashes");
    }

    @Test
    void hashPassword_shouldReturnNullForNullPassword() {
        String hashed = passwordHasher.hashPassword(null);
        assertNull(hashed, "Hashing a null password should return null");
    }

    @Test
    void verifyPassword_shouldReturnTrueForCorrectPassword() {
        String password = "correctPassword";
        String hashed = passwordHasher.hashPassword(password);

        assertTrue(passwordHasher.verifyPassword(password, hashed), "Verification should succeed for correct password");
    }

    @Test
    void verifyPassword_shouldReturnFalseForIncorrectPassword() {
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashed = passwordHasher.hashPassword(password);

        assertFalse(passwordHasher.verifyPassword(wrongPassword, hashed), "Verification should fail for incorrect password");
    }

    @Test
    void verifyPassword_shouldReturnFalseForNullOrEmptyPassword() {
        String password = "somePassword";
        String hashed = passwordHasher.hashPassword(password);

        assertFalse(passwordHasher.verifyPassword(null, hashed), "Verification should fail for null password");
        assertFalse(passwordHasher.verifyPassword("", hashed), "Verification should fail for empty password");
    }

    @Test
    void verifyPassword_shouldReturnFalseForNullOrEmptyHash() {
        String password = "somePassword";

        assertFalse(passwordHasher.verifyPassword(password, null), "Verification should fail for null hash");
        assertFalse(passwordHasher.verifyPassword(password, ""), "Verification should fail for empty hash");
    }
}
