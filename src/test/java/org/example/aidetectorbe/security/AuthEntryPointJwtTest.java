package org.example.aidetectorbe.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class AuthEntryPointJwtTest {

    private AuthEntryPointJwt authEntryPointJwt;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authEntryPointJwt = new AuthEntryPointJwt();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCommence_ShouldSendUnauthorizedError() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new AuthenticationException("Test exception") {};
        // When
        authEntryPointJwt.commence(request, response, authException);
        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo("application/json");
        String responseContent = response.getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        assertThat(jsonResponse.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(jsonResponse.get("message").asText()).isEqualTo("Test exception");
        assertThat(jsonResponse.get("status").asInt()).isEqualTo(401);
    }

    @Test
    public void testCommence_ShouldHandleNullRequestGracefully() throws Exception {
        // Given
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new AuthenticationException("Test exception") {};
        // When
        authEntryPointJwt.commence(null, response, authException);
        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo("application/json");
        String responseContent = response.getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        assertThat(jsonResponse.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(jsonResponse.get("message").asText()).isEqualTo("Test exception");
        assertThat(jsonResponse.get("status").asInt()).isEqualTo(401);
    }

    @Test
    public void testCommence_ShouldHandleNullResponseGracefully() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        AuthenticationException authException = new AuthenticationException("Test exception") {};
        // When
        try {
            authEntryPointJwt.commence(request, null, authException);
        } catch (NullPointerException e) {
            // Expected behavior since response is null
            // Then
            assertThat(e).isInstanceOf(NullPointerException.class);
        } catch (Exception e) {
            // Fail the test if any other exception occurs
            assertThat(true).isFalse();
        }
    }

    @Test
    public void testCommence_ShouldHandleNullAuthExceptionGracefully() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        // When
        authEntryPointJwt.commence(request, response, null);
        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo("application/json");
        String responseContent = response.getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        assertThat(jsonResponse.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(jsonResponse.get("message").asText()).isEqualTo("Authentication required");
        assertThat(jsonResponse.get("status").asInt()).isEqualTo(401);
    }
}