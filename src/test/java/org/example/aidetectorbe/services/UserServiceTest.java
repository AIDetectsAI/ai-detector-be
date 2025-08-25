package org.example.aidetectorbe.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
<<<<<<< Updated upstream

import java.util.UUID;

=======
import java.util.Optional;
import java.util.UUID;
import org.example.aidetectorbe.security.*;
import static net.bytebuddy.matcher.ElementMatchers.is;
>>>>>>> Stashed changes
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserServiceTest {
    @Test
    public void testCreateUser_GivenValidData_ShouldCorrectlySave() {
        UserRepository mockRepo = mock(UserRepository.class);
<<<<<<< Updated upstream
        UserService userService = new UserService(mockRepo);
=======
        PasswordHasher mockPasswordHasher = new PasswordHasher();
        JwtUtil mockUtil = mock(JwtUtil.class);

        UserService userService = new UserService(mockRepo, mockUtil, mockPasswordHasher);
>>>>>>> Stashed changes

        UserDTO dto = new UserDTO("login123", "pass123", "email@example.com");

        UUID uuid = UUID.randomUUID();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(mockRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(uuid); // symulacja wygenerowanego ID
            return user;
        });

        UUID id = userService.createUser(dto);

        verify(mockRepo).save(captor.capture());
        User savedUser = captor.getValue();

        assertEquals("login123", savedUser.getLogin());
        assertEquals("email@example.com", savedUser.getEmail());
        assertEquals(uuid, id);
    }
}
