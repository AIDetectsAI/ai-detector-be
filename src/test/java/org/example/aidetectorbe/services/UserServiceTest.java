package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.UserDTO;
import org.example.aidetectorbe.entities.Role;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.RoleRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.example.aidetectorbe.Constants.AI_DETECTOR_API_PROVIDER;
import static org.example.aidetectorbe.Constants.DEFAULT_USER_ROLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository mockUserRepository;
    private RoleRepository mockRoleRepository;
    private PasswordHasher mockPasswordHasher;
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockUserRepository = mock(UserRepository.class);
        mockRoleRepository = mock(RoleRepository.class);
        mockPasswordHasher = mock(PasswordHasher.class);
        JwtUtil jwtUtil = new JwtUtil();
        userService = new UserService(mockUserRepository, mockRoleRepository, mockPasswordHasher, jwtUtil);

        Role defaultRole = new Role();
        defaultRole.setName(DEFAULT_USER_ROLE);
        when(mockRoleRepository.findByName(DEFAULT_USER_ROLE)).thenReturn(Optional.of(defaultRole));
    }

    @Test
    void createDefaultUser_ShouldSaveUserWithCorrectData() {
        // Arrange
        UserDTO userDTO = new UserDTO("testUser", "password123", "test@example.com");
        UUID generatedId = UUID.randomUUID();
        when(mockPasswordHasher.hashPassword("password123")).thenReturn("hashedPassword");
        when(mockUserRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(generatedId);
            return user;
        });

        // Act
        UUID resultId = userService.createDefaultUser(userDTO);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("testUser", savedUser.getLogin());
        assertEquals("hashedPassword", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(generatedId, resultId);
        assertEquals(1, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().stream().anyMatch(role -> role.getName().equals(DEFAULT_USER_ROLE)));
    }

    @Test
    void createDefaultUser_ShouldThrowException_WhenRoleNotFound() {
        // Arrange
        when(mockRoleRepository.findByName(DEFAULT_USER_ROLE)).thenReturn(Optional.empty());
        UserDTO userDTO = new UserDTO("testUser", "password123", "test@example.com");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createDefaultUser(userDTO));
        assertEquals("Default role USER not found", exception.getMessage());
        verify(mockUserRepository, never()).save(any(User.class));
    }

    @Test
    void createDefaultUser_ShouldThrowException_WhenPasswordHashingFails() {
        // Arrange
        UserDTO userDTO = new UserDTO("testUser", "password123", "test@example.com");
        when(mockPasswordHasher.hashPassword("password123")).thenThrow(new RuntimeException("Hashing failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createDefaultUser(userDTO));
        assertEquals("Hashing failed", exception.getMessage());
        verify(mockUserRepository, never()).save(any(User.class));
    }

    @Test
    void createDefaultUser_ShouldAssignDefaultProvider() {
        // Arrange
        UserDTO userDTO = new UserDTO("testUser", "password123", "test@example.com");
        when(mockPasswordHasher.hashPassword("password123")).thenReturn("hashedPassword");
        when(mockUserRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.createDefaultUser(userDTO);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(AI_DETECTOR_API_PROVIDER, savedUser.getProvider());
    }

    @Test
    void verifyUserByLogin_returnsTrue_whenPasswordMatch(){
        // given
        UserDTO userDTO = new UserDTO("JohnParadox", "password", "mail@mail.mail");
        User user = new User();
        user.setLogin("login");
        user.setPassword("hashedPassword");

        // mocking
        when(mockPasswordHasher.hashPassword("password")).thenReturn("hashedPassword");
        when(mockUserRepository.findByLogin("JohnParadox")).thenReturn(Optional.of(user));

        // when
        boolean result = userService.verifyUserByLogin(userDTO);

        // then
        assertTrue(result);
    }

    @Test
    void verifyUserByLogin_returnsFalse_whenPasswordDontMatch(){
        // given
        UserDTO userDTO = new UserDTO("JohnParadox", "password", "mail@mail.mail");
        User user = new User();
        user.setLogin("login");
        user.setPassword("hashedPassword");

        // mocking
        when(mockPasswordHasher.hashPassword("password")).thenReturn("veryHashedPassword");
        when(mockUserRepository.findByLogin("JohnParadox")).thenReturn(Optional.of(user));

        // when
        boolean result = userService.verifyUserByLogin(userDTO);

        // then
        assertFalse(result);
    }

    @Test
    void verifyUserByLogin_returnsFalse_whenUserNotInRepository(){
        // given
        UserDTO userDTO = new UserDTO("JohnParadox", "password", "mail@mail.mail");

        // mocking
        when(mockUserRepository.findByLogin("login")).thenReturn(Optional.empty());

        // when
        boolean result = userService.verifyUserByLogin(userDTO);

        // then
        assertFalse(result);
    }
}