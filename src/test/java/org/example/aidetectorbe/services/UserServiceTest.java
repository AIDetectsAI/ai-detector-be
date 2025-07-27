package org.example.aidetectorbe.services;

import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void testCreateUser_GivenValidData_ShouldCorrectlySave() {
        UserRepository mockRepo = mock(UserRepository.class);
        PasswordHasher mockHasher = mock(PasswordHasher.class);
        UserService userService = new UserService(mockRepo, mockHasher);

        UserDTO dto = new UserDTO("login123", "pass123", "email@example.com");

        UUID uuid = UUID.randomUUID();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(mockRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(uuid);
            return user;
        });

        UUID id = userService.createUser(dto);

        verify(mockRepo).save(captor.capture());
        User savedUser = captor.getValue();

        assertEquals("login123", savedUser.getLogin());
        assertEquals("email@example.com", savedUser.getEmail());
        assertEquals(mockHasher.hashPassword("pass123"), savedUser.getPassword());
        assertEquals(uuid, id);
    }
}
