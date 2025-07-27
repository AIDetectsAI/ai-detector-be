package org.example.aidetectorbe.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.aidetectorbe.security.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {

        if(result.hasErrors()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("invalid data: " + result.getAllErrors());
        }
        UUID userId = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDTO xUser){
        Optional<User> userOptional = userService.getUserByLogin(xUser.getLogin());
        if(userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("Login does not exist");
        }
        User user = userOptional.get();
        if(!userService.encryptPassword(xUser.getPassword()).equals(user.getPassword())){
            return ResponseEntity.status(401).body("Invalid password");
        }
        String token = userService.generateToken(xUser.getLogin());
        return ResponseEntity.ok(Map.of("token", token));
    }
}