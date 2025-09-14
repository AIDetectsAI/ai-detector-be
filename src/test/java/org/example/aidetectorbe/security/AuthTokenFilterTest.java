package org.example.aidetectorbe.security;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

public class AuthTokenFilterTest {
    
    private AuthTokenFilter authTokenFilter;

    @BeforeEach
    void setUp() {
        authTokenFilter = new AuthTokenFilter();
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
    public void doFilterInternal_ShouldNotAuthorize_WhenJwtValidationFails() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abcdefg");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();
        // When
        try {
            authTokenFilter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            assertFalse(true);
        }
        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
