package org.example.aidetectorbe.services;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.logger.Log;
import org.example.aidetectorbe.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UUID createUser(UserDTO userDTO) {
        User user = new User(userDTO.getLogin(), passwordHasher.hashPassword(userDTO.getPassword()), userDTO.getEmail());
        userRepository.save(user);
        Log.info("Saved user with id " + user.getId());
        return user.getId();
    }
}
