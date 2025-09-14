package org.example.aidetectorbe.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class AuthTokenFilterTest {
    
    private AuthTokenFilter authTokenFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authTokenFilter = new AuthTokenFilter();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testAuthTokenFilter_IsNotNull() {
        //Then
        assertThat(authTokenFilter).isNotNull();
    }

    @Test
    public void doFilterInternal_ShouldHandleRequest() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();
        // When
        try {
            authTokenFilter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            assertThat(true).isFalse();
        }
        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    public void parseJwt_ShouldReturnNull_WhenNoAuthorizationHeader() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        // When
        String result = authTokenFilter.parseJwt(request);
        // Then
        assertThat(result).isNull();
    }

    @Test
    public void parseJwt_ShouldReturnNull_WhenAuthorizationHeaderDoesNotStartWithBearer() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abcdefg");
        // When
        String result = authTokenFilter.parseJwt(request);
        // Then
        assertThat(result).isNull();
    }

    @Test
    public void parseJwt_ShouldReturnToken_WhenAuthorizationHeaderStartsWithBearer() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abcdefg");
        // When
        String result = authTokenFilter.parseJwt(request);
        // Then
        assertThat(result).isEqualTo("abcdefg");
    }

    @Test
    public void doFilterInternal_ShouldNotAuthorize_WhenJwtValidationFails() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abcdefg");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();
        
        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo("application/json");
        
        String responseContent = response.getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        
        assertThat(jsonResponse.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(jsonResponse.get("status").asInt()).isEqualTo(401);
        assertThat(jsonResponse.has("message")).isTrue();
    }
}
