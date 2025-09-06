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
        UserDTO userDTO = new UserDTO("login123", "pass123", "email@example.com");
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
    void testCreateUser_GivenNullLogin_ShouldReturnBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO(null, "pass123", "email@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("login cannot be blank")));

        verify(mockUserService, never()).createDefaultUser(any());
    }

    @Test
    void testCreateUser_GivenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO("login123", "pass123", "invalid-email");

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
        UserDTO userDTO = new UserDTO("JohnParadox", "password", "mail@mail.mail");

        // mock
        when(mockUserService.verifyUserByLogin(userDTO)).thenReturn(true);
        when(mockUserService.getTokenByLogin("JohnParadox")).thenReturn("mocked_token");

        // when n then
        mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"mocked_token\"}"));

        verify(mockUserService).verifyUserByLogin(userDTO);
        verify(mockUserService).getTokenByLogin("JohnParadox");
    }

    @Test
    void testLoginUser_GivenIncorrectCredentials_ShouldReturnUnauthorized() throws Exception {
        // given
        UserDTO userDTO = new UserDTO("JohnParadox", "password", "mail@mail.mail");

        // mock
        when(mockUserService.verifyUserByLogin(userDTO)).thenReturn(false);

        // when n then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User does not exist or invalid password"));

        verify(mockUserService).verifyUserByLogin(userDTO);
        verify(mockUserService, never()).getTokenByLogin(anyString());
    }
}
