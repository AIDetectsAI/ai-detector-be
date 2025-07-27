package org.example.aidetectorbe.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.logger.Log;
import org.example.aidetectorbe.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        Log.info("Received a request to register a new user");
        if(result.hasErrors()) {
            Log.error("Request contained invalid data");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("invalid data: " + result.getAllErrors());
        }
        UUID userId = userService.createUser(userDTO);
        Log.info("User with id " + userId + " has been created");
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }
}
