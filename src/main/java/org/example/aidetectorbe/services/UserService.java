package org.example.aidetectorbe.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.Role;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.RoleRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import static org.example.aidetectorbe.utils.Constants.AI_DETECTOR_API_PROVIDER;
import static org.example.aidetectorbe.utils.Constants.DEFAULT_USER_ROLE;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final JwtUtil jwtUtil;

    public UUID createDefaultUser(UserDTO userDTO) {
        Set<Role> roles = new java.util.HashSet<>();
        roles.add(roleRepository.findByName(DEFAULT_USER_ROLE)
                .orElseThrow(() -> new RuntimeException("Default role USER not found")));
        User user = new User(userDTO.getLogin(), passwordHasher.hashPassword(userDTO.getPassword()), userDTO.getEmail(),
                AI_DETECTOR_API_PROVIDER, null, roles);
        userRepository.save(user);
        return user.getId();
    }

    public boolean existsByLoginAndProvider(String login, String provider) {
        return userRepository.findByLoginAndProvider(login, provider).isPresent();
    }

    public boolean verifyUserByLoginAndProvider(UserDTO userDTO, String provider) {
        return verifyUserByLoginAndProvider(userDTO.getLogin(), userDTO.getPassword(), provider);
    }

    public boolean verifyUserByLoginAndProvider(String login, String password, String provider) {
        User user = userRepository.findByLoginAndProvider(login, provider).orElse(null);
        if (user == null) {
            return false;
        }
        if (!user.getProvider().equals(provider)) {
            return false;
        }
        return passwordHasher.verifyPassword(password, user.getPassword());
    }

    public String getTokenByLogin(String login) {
        return jwtUtil.generateToken(login);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    @Transactional
    public void changePassword(String login, String currentPassword, String newPassword) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!AI_DETECTOR_API_PROVIDER.equals(user.getProvider())) {
            throw new IllegalStateException("Password change is only available for local accounts");
        }

        if (!passwordHasher.verifyPassword(currentPassword, user.getPassword())) {
            throw new SecurityException("Invalid current password");
        }

        user.setPassword(passwordHasher.hashPassword(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUserByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
    }
}