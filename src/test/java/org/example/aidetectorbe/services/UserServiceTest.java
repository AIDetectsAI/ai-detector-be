package org.example.aidetectorbe.services;

import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void testCreateUser_GivenValidData_ShouldCorrectlySave() {
        UserRepository mockRepo = mock(UserRepository.class);
        UserService userService = new UserService(mockRepo);

        UserDTO dto = new UserDTO("login123", "pass123", "email@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(mockRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42); // symulacja wygenerowanego ID
            return user;
        });

        int id = userService.createUser(dto);

        verify(mockRepo).save(captor.capture());
        User savedUser = captor.getValue();

        assertEquals("login123", savedUser.getLogin());
        assertEquals("email@example.com", savedUser.getEmail());
        assertEquals(42, id);
    }

}
