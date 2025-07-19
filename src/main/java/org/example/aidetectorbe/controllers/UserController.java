package org.example.aidetectorbe.controllers;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Integer> createUser(@RequestBody UserDTO userDTO) {
        int result = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
