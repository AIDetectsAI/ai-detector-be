package org.example.aidetectorbe.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.ChangePasswordRequest;
import org.example.aidetectorbe.dto.ErrorResponse;
import org.example.aidetectorbe.dto.LoginDTO;
import org.example.aidetectorbe.dto.LoginResponse;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.utils.logger.Log;
import org.example.aidetectorbe.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import java.util.Map;
import static org.example.aidetectorbe.utils.Constants.AI_DETECTOR_API_PROVIDER;

@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        Log.info("Received a request to register a new user");
        if (result.hasErrors()) {
            Log.error("Register request contained invalid data");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("invalid data: " + result.getAllErrors());
        }

        if (userService.existsByLoginAndProvider(userDTO.getLogin(), AI_DETECTOR_API_PROVIDER)) {
            Log.error("Register request failed: user with login already exists");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User with this login already exists");
        }

        UUID userId = userService.createDefaultUser(userDTO);
        Log.info("User with id " + userId + " has been created");
        String message = "User with login " + userDTO.getLogin() + " has been created";
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO, BindingResult result) {
        Log.info(String.format("Attempting to log in with login '%s'", loginDTO.getLogin()));
        if (result.hasErrors()) {
            Log.error("Login request contained invalid data");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("invalid data: " + result.getAllErrors());
        }
        if (!userService.verifyUserByLoginAndProvider(loginDTO.getLogin(), loginDTO.getPassword(),
                AI_DETECTOR_API_PROVIDER)) {
            Log.info(String.format("Invalid password for attempted login as '%s'", loginDTO.getLogin()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User does not exist or invalid password");
        }
        String token = userService.getTokenByLogin(loginDTO.getLogin());
        Log.info(String.format("Successful log in for user '%s'", loginDTO.getLogin()));
        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(token));
    }

    @GetMapping("/api/me")
    public ResponseEntity<?> getCurrentUser(jakarta.servlet.http.HttpServletRequest request) {
        String login = (String) request.getAttribute("login");
        if (login == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        var user = userService.findByLogin(login);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(Map.of(
                "login", user.getLogin(),
                "email", user.getEmail()));
    }

    @PutMapping("/api/me/password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            BindingResult result,
            jakarta.servlet.http.HttpServletRequest request) {
        String login = (String) request.getAttribute("login");
        if (login == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (result.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse("Bad Request", "invalid data: " + result.getAllErrors(),
                    400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            userService.changePassword(login, changePasswordRequest.getCurrentPassword(),
                    changePasswordRequest.getNewPassword());
            Log.info(String.format("Password changed successfully for user '%s'", login));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse("Not Found", e.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (SecurityException e) {
            ErrorResponse errorResponse = new ErrorResponse("Unauthorized", e.getMessage(), 401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("Bad Request", e.getMessage(), 400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Log.error("Unexpected error while changing password", e);
            ErrorResponse errorResponse = new ErrorResponse("Internal Server Error", "Failed to change password", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/api/me")
    public ResponseEntity<?> deleteCurrentUser(jakarta.servlet.http.HttpServletRequest request) {
        String login = (String) request.getAttribute("login");
        if (login == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        try {
            userService.deleteUserByLogin(login);
            Log.info(String.format("User '%s' has been deleted", login));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse("Not Found", e.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Log.error("Unexpected error while deleting user", e);
            ErrorResponse errorResponse = new ErrorResponse("Internal Server Error", "Failed to delete account", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
