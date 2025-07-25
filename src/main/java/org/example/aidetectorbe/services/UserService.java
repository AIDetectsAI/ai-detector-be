package org.example.aidetectorbe.services;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UUID createUser(UserDTO userDTO) {

        User user = new User(userDTO.getLogin(), userDTO.getPassword(), userDTO.getEmail());
        userRepository.save(user);
        return user.getId();
    }
}
