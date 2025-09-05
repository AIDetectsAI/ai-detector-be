package org.example.aidetectorbe.services;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.entities.Role;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.logger.Log;
import org.example.aidetectorbe.repository.RoleRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.example.aidetectorbe.Constants.GITHUB_API_PROVIDER;

@Service
@AllArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestTemplate restTemplate;

    protected OAuth2User delegateLoadUser(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        Log.info("Loading user from OAuth2 provider: " + request.getClientRegistration().getRegistrationId());

        Set<Role> roles = Set.of(
                roleRepository.findByName("USER")
                        .orElseThrow(() -> new RuntimeException("Default role USER not found"))
        );

        OAuth2User oAuth2User = delegateLoadUser(request);
        String providerId = request.getClientRegistration().getRegistrationId();
        String loginName;
        String email;

        if (GITHUB_API_PROVIDER.equals(providerId)) {
            Log.info("Processing GitHub OAuth2 user");
            loginName = oAuth2User.getAttribute("login");
            email = oAuth2User.getAttribute("email");

            if (email == null) {
                String token = request.getAccessToken().getTokenValue();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<List> response = restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        HttpMethod.GET,
                        entity,
                        List.class
                );

                List<Map<String, Object>> emails = response.getBody();
                if (emails != null) {
                    for (Map<String, Object> e : emails) {
                        Boolean primary = (Boolean) e.get("primary");
                        Boolean verified = (Boolean) e.get("verified");
                        String emailStr = (String) e.get("email");
                        if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                            email = emailStr;
                            break;
                        }
                    }
                    Log.info("Fetched emails from GitHub API: " + emails);
                }

                if (email == null) {
                    Log.warn("No primary verified email found for GitHub user, using fallback email");
                    email = loginName + "@github.com";
                }
            }
        } else {
            loginName = oAuth2User.getAttribute("name");
            email = oAuth2User.getAttribute("email");
        }

        String providerUserId = oAuth2User.getName();
        String finalEmail = email;

        userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setLogin(loginName);
            newUser.setEmail(finalEmail);
            newUser.setIsDeleted(false);
            newUser.setProvider(providerId);
            newUser.setProviderUserId(providerUserId);
            newUser.setRoles(roles);
            return userRepository.save(newUser);
        });

        Log.info("User " + loginName + " with email " + finalEmail + " processed successfully");
        return oAuth2User;
    }
}