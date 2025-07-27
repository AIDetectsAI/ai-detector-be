package org.example.aidetectorbe.services;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.example.aidetectorbe.security.Encoder;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Encoder encoder;

    public UUID createUser(UserDTO userDTO) {

        User user = new User(userDTO.getLogin(), userDTO.getPassword(), userDTO.getEmail());
        userRepository.save(user);
        return user.getId();
    }

    public String generateToken(String login){
        return jwtUtil.generateToken(login);
    }

    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public String encryptPassword(String password){
        return encoder.encryptPassword(password);
    }
}
