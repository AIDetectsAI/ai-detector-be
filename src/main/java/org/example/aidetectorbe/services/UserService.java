package org.example.aidetectorbe.services;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.Role;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.RoleRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.security.JwtUtil;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.UUID;
import static org.example.aidetectorbe.Constants.AI_DETECTOR_API_PROVIDER;
import static org.example.aidetectorbe.Constants.DEFAULT_USER_ROLE;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final JwtUtil jwtUtil;

    public UUID createDefaultUser(UserDTO userDTO) {
        Set<Role> roles = new java.util.HashSet<>();
        roles.add(roleRepository.findByName(DEFAULT_USER_ROLE).orElseThrow(() -> new RuntimeException("Default role USER not found")));
        User user = new User(userDTO.getLogin(), passwordHasher.hashPassword(userDTO.getPassword()), userDTO.getEmail(), AI_DETECTOR_API_PROVIDER, null, roles);
        userRepository.save(user);
        return user.getId();
    }

    public boolean verifyUserByLogin(UserDTO userDTO){
        User user = userRepository.findByLogin(userDTO.getLogin()).orElse(null);
        if(user == null){
            return false;
        }
        return passwordHasher.hashPassword(userDTO.getPassword()).equals(user.getPassword());
    }

    public String getTokenByLogin(String login){
        return jwtUtil.generateToken(login);
    }
}
