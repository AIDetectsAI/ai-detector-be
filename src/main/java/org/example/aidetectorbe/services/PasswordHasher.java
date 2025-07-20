package org.example.aidetectorbe.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.example.aidetectorbe.logger.Log;
import org.springframework.stereotype.Service;

@Service
public class PasswordHasher {

    private final Argon2 argon2;

    public PasswordHasher() {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64);
    }

    public String hashPassword(String password) {
        if (password == null) {
            Log.error("Password is null and cannot be hashed.");
            return null;
        }
        return argon2.hash(2, 65536, 1, password.toCharArray());
    }

    public boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || password.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        return argon2.verify(hashedPassword, password.toCharArray());
    }
}
