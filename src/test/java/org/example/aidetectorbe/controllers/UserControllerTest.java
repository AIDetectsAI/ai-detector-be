package org.example.aidetectorbe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aidetectorbe.dto.UserDTO;
<<<<<<< Updated upstream
=======
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.security.JwtUtil;
import org.example.aidetectorbe.services.PasswordHasher;
>>>>>>> Stashed changes
import org.example.aidetectorbe.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

<<<<<<< Updated upstream
=======
import static net.bytebuddy.matcher.ElementMatchers.is;
>>>>>>> Stashed changes
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
<<<<<<< Updated upstream

=======
import org.example.aidetectorbe.entities.User;
import java.util.Optional;
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
>>>>>>> Stashed changes
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private UserService mockUserService;
<<<<<<< Updated upstream

=======
    @Autowired
>>>>>>> Stashed changes
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    /*void setUp() {
        mockUserService = mock(UserService.class);
        UserController userController = new UserController(mockUserService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }*/

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

<<<<<<< Updated upstream
=======
    @Test
    void testLoginUser_GivenCorrectCredentials_ShouldReturnToken() throws Exception {
        UserDTO userDTO = new UserDTO("user", "password", null);
        User user = new User("user", "hashed-password", "user@test.com");
        String expectedToken = "mocked-jwt-token";

        Mockito.when(mockUserService.getUserByLogin("user")).thenReturn(Optional.of(user));
        Mockito.when(mockUserService.verifyPassword("password", "hashed-password")).thenReturn(true);
        Mockito.when(mockUserService.generateToken("user")).thenReturn(expectedToken);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.token").value(expectedToken));
    }
>>>>>>> Stashed changes
}
