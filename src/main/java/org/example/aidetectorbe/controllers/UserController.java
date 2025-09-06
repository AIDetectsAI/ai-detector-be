package org.example.aidetectorbe.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.LoginResponse;
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
@RequestMapping("/auth")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        Log.info("Received a request to register a new user");
        if (result.hasErrors()) {
            Log.error("Request contained invalid data");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("invalid data: " + result.getAllErrors());
        }
        UUID userId = userService.createDefaultUser(userDTO);
        Log.info("User with id " + userId + " has been created");
        String message = "User with login " + userDTO.getLogin() + " has been created";
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserDTO userDTO){
        Log.info(String.format("User '%s' is requesting to log in", userDTO.getLogin()));
        if(!userService.verifyUserByLogin(userDTO)){
            Log.info(String.format("Invalid password for log in to user '%s'", userDTO.getLogin()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User does not exist or invalid password");
        }
        String token = userService.getTokenByLogin(userDTO.getLogin());
        Log.info(String.format("Successful log in for user '%s'", userDTO.getLogin()));
        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(token));
    }
}
