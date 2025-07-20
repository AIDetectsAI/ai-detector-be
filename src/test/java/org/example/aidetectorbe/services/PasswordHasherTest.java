package org.example.aidetectorbe.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashPassword_shouldReturnHashedString() {
        String password = "testPassword123";
        String hashed = PasswordHasher.hashPassword(password);

        assertNotNull(hashed, "Hashed password should not be null");
        assertNotEquals(password, hashed, "Hashed password should not equal the original password");
    }

    @Test
    void hashPassword_shouldReturnDifferentHashesForSamePassword() {
        String password = "testPassword123";
        String hashed1 = PasswordHasher.hashPassword(password);
        String hashed2 = PasswordHasher.hashPassword(password);

        assertNotEquals(hashed1, hashed2, "Hashing the same password should produce different hashes");
    }

    @Test
    void verifyPassword_shouldReturnTrueForCorrectPassword() {
        String password = "correctPassword";
        String hashed = PasswordHasher.hashPassword(password);

        assertTrue(PasswordHasher.verifyPassword(password, hashed), "Verification should succeed for correct password");
    }


    @Test
    void verifyPassword_shouldReturnFalseForIncorrectPassword() {
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashed = PasswordHasher.hashPassword(password);

        assertFalse(PasswordHasher.verifyPassword(wrongPassword, hashed), "Verification should fail for incorrect password");
    }

    @Test
    void verifyPassword_shouldReturnFalseForNullOrEmptyPassword() {
        String password = "somePassword";
        String hashed = PasswordHasher.hashPassword(password);

        assertFalse(PasswordHasher.verifyPassword(null, hashed), "Verification should fail for null password");
        assertFalse(PasswordHasher.verifyPassword("", hashed), "Verification should fail for empty password");
    }
}
