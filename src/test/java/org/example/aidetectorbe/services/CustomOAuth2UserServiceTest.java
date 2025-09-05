package org.example.aidetectorbe.services;

import org.example.aidetectorbe.entities.Role;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.RoleRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.example.aidetectorbe.Constants.DEFAULT_USER_ROLE;
import static org.example.aidetectorbe.Constants.GITHUB_API_PROVIDER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Role defaultRole = new Role();
        defaultRole.setName(DEFAULT_USER_ROLE);
        when(roleRepository.findByName(DEFAULT_USER_ROLE)).thenReturn(Optional.of(defaultRole));
    }

    private OAuth2UserRequest buildRequest(String provider) {
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(clientRegistration.getRegistrationId()).thenReturn(provider);

        OAuth2AccessToken token = mock(OAuth2AccessToken.class);
        when(token.getTokenValue()).thenReturn("fake-token");

        return new OAuth2UserRequest(clientRegistration, token);
    }

    @Test
    void testLoadUser_GitHub_WithEmail_attributePresent() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("login", "testuser");
        attributes.put("email", "test@example.com");
        OAuth2User providerUser = new DefaultOAuth2User(Collections.emptySet(), attributes, "login");

        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(providerUser).when(spyService).delegateLoadUser(any(OAuth2UserRequest.class));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2UserRequest request = buildRequest(GITHUB_API_PROVIDER);

        // when
        OAuth2User result = spyService.loadUser(request);

        // then
        assertEquals("testuser", result.getAttribute("login"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoadUser_GitHub_NoEmail_PrimaryVerifiedFetched_viaRestTemplate() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("login", "gituser");
        attributes.put("email", null);
        OAuth2User providerUser = new DefaultOAuth2User(Collections.emptySet(), attributes, "login");

        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(providerUser).when(spyService).delegateLoadUser(any(OAuth2UserRequest.class));

        List<Map<String, Object>> emails = List.of(
                Map.of("email", "primary@example.com", "primary", true, "verified", true)
        );
        when(restTemplate.exchange(
                eq("https://api.github.com/user/emails"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenReturn(ResponseEntity.ok(emails));

        when(userRepository.findByEmail("primary@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2UserRequest request = buildRequest(GITHUB_API_PROVIDER);

        // when
        OAuth2User result = spyService.loadUser(request);

        // then
        assertEquals("gituser", result.getAttribute("login"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoadUser_GitHub_NoEmail_NoVerifiedEmails_fallbackToLoginAtGithub() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("login", "nofetched");
        attributes.put("email", null);
        OAuth2User providerUser = new DefaultOAuth2User(Collections.emptySet(), attributes, "login");

        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(providerUser).when(spyService).delegateLoadUser(any(OAuth2UserRequest.class));

        when(restTemplate.exchange(
                eq("https://api.github.com/user/emails"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        when(userRepository.findByEmail("nofetched@github.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2UserRequest request = buildRequest(GITHUB_API_PROVIDER);

        OAuth2User result = spyService.loadUser(request);

        assertEquals("nofetched", result.getAttribute("login"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoadUser_OtherProvider_savesUser() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "googleuser");
        attributes.put("email", "google@example.com");
        OAuth2User providerUser = new DefaultOAuth2User(Collections.emptySet(), attributes, "name");

        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(providerUser).when(spyService).delegateLoadUser(any(OAuth2UserRequest.class));

        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2UserRequest request = buildRequest("google");

        OAuth2User result = spyService.loadUser(request);

        assertEquals("googleuser", result.getAttribute("name"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoadUser_NoDefaultRole_throwsRuntimeException() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        OAuth2UserRequest request = buildRequest("google");
        assertThrows(RuntimeException.class, () -> customOAuth2UserService.loadUser(request));
    }
}