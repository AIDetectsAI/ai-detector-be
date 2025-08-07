package org.example.aidetectorbe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.security.Encoder;
import org.example.aidetectorbe.security.JwtUtil;
import org.example.aidetectorbe.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.example.aidetectorbe.entities.User;
import java.util.Optional;

public class UserControllerTest {

    private MockMvc mockMvc;
    private UserService mockUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockUserService = mock(UserService.class);
        UserController userController = new UserController(mockUserService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testCreateUser_GivenValidData_ShouldReturnCreatedStatus() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO("login123", "pass123", "email@example.com");
        UUID uuid = UUID.randomUUID();
        when(mockUserService.createUser(any(UserDTO.class))).thenReturn(uuid);

        // When + Then
        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + uuid.toString() + "\""));

        verify(mockUserService).createUser(any(UserDTO.class));
    }

    @Test
    void testCreateUser_GivenNullLogin_ShouldReturnBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO(null, "pass123", "email@example.com");

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("login cannot be blank")));

        verify(mockUserService, never()).createUser(any());
    }

    @Test
    void testCreateUser_GivenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO("login123", "pass123", "invalid-email");

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("email invalid or blank")));

        verify(mockUserService, never()).createUser(any());
    }

    @Test
    void testLogin_GivenCorrectData_ShouldReturnOk() throws Exception {
        UserDTO xUser = new UserDTO("login", "password", null);
        String json = objectMapper.writeValueAsString(xUser);
        User mockUser = new User(UUID.randomUUID(), "login", "encrypted-password", null);

        when(mockUserService.getUserByLogin("login")).thenReturn(Optional.of(mockUser));
        when(mockUserService.encryptPassword("password")).thenReturn("ecnrypted-password");
        when(mockUserService.generateToken("login")).thenReturn("mock-token");

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }
}
