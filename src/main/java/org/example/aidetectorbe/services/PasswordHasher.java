package org.example.aidetectorbe.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.example.aidetectorbe.logger.Log;

public class PasswordHasher {
    static Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64);

    public static String hashPassword(String password) {
        try {
            return argon2.hash(2, 65536, 1, password.toCharArray());
        } catch (NullPointerException e) {
            Log.error("Password is null and cannot be hashed.", e);
            return null;
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || password.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        return argon2.verify(hashedPassword, password.toCharArray());
    }
}
