package org.example.aidetectorbe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.example.aidetectorbe.utils.Constants.AI_DETECTOR_API_PROVIDER;
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
        UserDTO userDTO = new UserDTO("login123", "Passw0rd!", "email@example.com");
        UUID uuid = UUID.randomUUID();
        when(mockUserService.createDefaultUser(any(UserDTO.class))).thenReturn(uuid);
        String message = "User with login " + userDTO.getLogin() + " has been created";


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string(message));

        verify(mockUserService).createDefaultUser(any(UserDTO.class));
    }

    @Test
    void testCreateUser_GivenExistingLogin_ShouldReturnConflict() throws Exception {
        UserDTO userDTO = new UserDTO("login123", "Passw0rd!", "email@example.com");
        when(mockUserService.existsByLoginAndProvider(userDTO.getLogin(), AI_DETECTOR_API_PROVIDER)).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().string("User with this login already exists"));

        verify(mockUserService, never()).createDefaultUser(any(UserDTO.class));
    }

    @Test
    void testCreateUser_GivenNullLogin_ShouldReturnBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO(null, "Passw0rd!", "email@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("login cannot be blank")));

        verify(mockUserService, never()).createDefaultUser(any());
    }

    @Test
    void testCreateUser_GivenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO("login123", "Passw0rd!", "invalid-email");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("email invalid or blank")));

        verify(mockUserService, never()).createDefaultUser(any());
    }

    @Test
    void testLoginUser_GivenCorrectCredentials_ShouldReturnToken() throws Exception {
        // given
        UserDTO userDTO = new UserDTO("JohnParadox", "Passw0rd!", "mail@mail.mail");

        // mock
        when(mockUserService.verifyUserByLoginAndProvider(userDTO, AI_DETECTOR_API_PROVIDER)).thenReturn(true);
        when(mockUserService.getTokenByLogin("JohnParadox")).thenReturn("mocked_token");

        // when n then
        mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"mocked_token\"}"));

        verify(mockUserService).verifyUserByLoginAndProvider(userDTO, AI_DETECTOR_API_PROVIDER);
        verify(mockUserService).getTokenByLogin("JohnParadox");
    }

    @Test
    void testLoginUser_GivenIncorrectCredentials_ShouldReturnUnauthorized() throws Exception {
        // given
        UserDTO userDTO = new UserDTO("JohnParadox", "Passw0rd!", "mail@mail.mail");

        // mock
        when(mockUserService.verifyUserByLoginAndProvider(userDTO, AI_DETECTOR_API_PROVIDER)).thenReturn(false);

        // when n then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User does not exist or invalid password"));

        verify(mockUserService).verifyUserByLoginAndProvider(userDTO, AI_DETECTOR_API_PROVIDER);
        verify(mockUserService, never()).getTokenByLogin(anyString());
    }

    @Test
    void testLoginUser_GivenNullLogin_ShouldReturnBadRequest() throws Exception {
        String bad_json = """
        "password" : "pass",
        "email" : "mail@mail.com"
        """;
        mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bad_json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginUser_GivenNullPassword_ShouldReturnBadRequest() throws Exception {
        String bad_json = """
        "login" : "JohnParadox",
        "email" : "mail@mail.com"
        """;
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bad_json))
                .andExpect(status().isBadRequest());
    }
}
