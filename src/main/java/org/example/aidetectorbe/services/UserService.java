package org.example.aidetectorbe.services;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public int createUser(UserDTO userDTO) {

        User user = new User(userDTO.getLogin(), userDTO.getPassword(), userDTO.getEmail());
        userRepository.save(user);
        System.out.println("Saved user: " + userDTO);
        return user.getId();
    }
}
