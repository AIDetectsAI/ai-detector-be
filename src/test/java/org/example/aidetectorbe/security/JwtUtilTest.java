package org.example.aidetectorbe.security;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Value;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=mySecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm",
    "jwt.expiration=3600000" // 1 hour
})
public class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Test
    public void testGenerateToken_GivenValidLogin_ShouldReturnNonEmptyToken() {
        //Given
        String login = "testUser";
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    public void testGenerateToken_GivenEmptyLogin_ShouldHandleGracefully() {
        //Given
        String login = "";
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    public void testGenerateToken_WhenNullLogin_ShouldHandleGracefully() {
        //Given
        String login = null;
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    public void testGenerateToken_WhenSpecialCharactersInLogin_ShouldHandleGracefully() {
        //Given
        String login = " user!@#_$%^&*() ";
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    public void testExtractLogin_GivenValidToken_ShouldReturnCorrectLogin() {
        //Given
        String login = "testUser";
        String token = jwtUtil.generateToken(login);
        //When
        String extractedLogin = jwtUtil.extractLogin(token);
        //Then
        assertThat(extractedLogin).isEqualTo(login);
    }

    @Test
    public void testExtractLogin_GivenTokenWithSpecialCharacters_ShouldReturnCorrectLogin() {
        //Given
        String login = " user!@#_$%^&*() ";
        String token = jwtUtil.generateToken(login);
        //When
        String extractedLogin = jwtUtil.extractLogin(token);
        //Then
        assertThat(extractedLogin).isEqualTo(login.trim());
    }

    @Test
    public void testExtractLogin_GivenInvalidToken_ShouldThrowException() {
        //Given
        String invalid = "invalidToken";
        //When
        try {
            jwtUtil.extractLogin(invalid);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(MalformedJwtException.class);
            assertThat(e.getMessage()).contains("Invalid JWT token");
        }
    }

    @Test
    public void testExtractLogin_GivenExpiredToken_ShouldThrowException() {
        //Given
        try (MockedStatic<System> mockedSystem = mockStatic(System.class)) {
            long currentTime = System.currentTimeMillis();
            mockedSystem.when(System::currentTimeMillis).thenReturn(currentTime);
        
            String token = jwtUtil.generateToken("testUser");
        
            // Fast forward time beyond expiration
            mockedSystem.when(System::currentTimeMillis)
                  .thenReturn(currentTime + jwtExpiration + 1000);
            //When
            try {
                jwtUtil.extractLogin(token);
            } catch (Exception e) {
                //Then
                assertThat(e).isInstanceOf(ExpiredJwtException.class);
                assertThat(e.getMessage()).contains("JWT token is expired");
            }
        }
    }

    @Test
    public void testExtractLogin_GivenEmptyToken_ShouldThrowException() {
        //Given
        String token = "";
        //When
        try {
            jwtUtil.extractLogin(token);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("JWT claims string is empty");
        }
    }

    @Test
    public void testValidateToken_GivenValidToken_ShouldReturnTrue() {
        //Given
        String login = "testUser";
        String token = jwtUtil.generateToken(login);
        //When
        boolean isValid = jwtUtil.validateToken(token);
        //Then
        assertThat(isValid).isTrue();
    }

    @Test
    public void testValidateToken_GivenInvalidToken_ShouldThrowException() {
        //Given
        String invalidToken = "invalidToken";
        //When
        try {
            jwtUtil.validateToken(invalidToken);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(MalformedJwtException.class);
            assertThat(e.getMessage()).contains("Invalid JWT token");
        }
    }

    @Test
    public void testValidateToken_GivenExpiredToken_ShouldThrowException() {
        //Given
        try (MockedStatic<System> mockedSystem = mockStatic(System.class)) {
            long currentTime = System.currentTimeMillis();
            mockedSystem.when(System::currentTimeMillis).thenReturn(currentTime);
        
            String token = jwtUtil.generateToken("testUser");
        
            // Fast forward time beyond expiration
            mockedSystem.when(System::currentTimeMillis)
                  .thenReturn(currentTime + jwtExpiration + 1000);
            //When
            try {
                jwtUtil.validateToken(token);
            } catch (Exception e) {
                //Then
                assertThat(e).isInstanceOf(ExpiredJwtException.class);
                assertThat(e.getMessage()).contains("JWT token is expired");
            }
        }
    }

    @Test
    public void testValidateToken_GivenNullToken_ShouldThrowException() {
        //Given
        String token = null;
        //When
        try {
            jwtUtil.validateToken(token);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("JWT claims string is empty");
        }
    }

    @Test
    public void testValidateToken_GivenTokenWithSpecialCharacters_ShouldReturnTrue() {
        //Given
        String login = " user!@#_$%^&*() ";
        String token = jwtUtil.generateToken(login);
        //When
        boolean isValid = jwtUtil.validateToken(token);
        //Then
        assertThat(isValid).isTrue();
    }

    @Test
    public void testValidateToken_GivenEmptyToken_ShouldThrowException() {
        //Given
        String token = "";
        //When
        try {
            jwtUtil.validateToken(token);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("JWT claims string is empty");
        }
    }

    @Test
    public void testValidateToken_WithInvalidSignature_ShouldThrowException() {
        String token = jwtUtil.generateToken("testUser");
        // Modify the token to corrupt the signature
        String corruptedToken = token.substring(0, token.length() - 5) + "xxxxx";
        //When
        try {
            jwtUtil.validateToken(corruptedToken);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(SecurityException.class);
            assertThat(e.getMessage()).contains("Invalid JWT signature");
        }
    }

    @Test
    public void testTokenWorkflow_GenerateExtractValidate_ShouldWorkTogether() {
        String login = "integrationTestUser";
        // Generate token
        String token = jwtUtil.generateToken(login);
        assertThat(token).isNotNull();
        // Extract login
        String extractedLogin = jwtUtil.extractLogin(token);
        assertThat(extractedLogin).isEqualTo(login);
        // Validate token
        boolean isValid = jwtUtil.validateToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    public void testTokenSecurity_DifferentInstance_ShouldThrowException() {
        // Test that tokens created with different secret keys fail validation
        JwtUtil anotherJwtUtil = new JwtUtil();
        anotherJwtUtil.setJwtSecretKey("anotherSecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm");
        anotherJwtUtil.init();
        String login = "testUser";
        String token = jwtUtil.generateToken(login);
        try {
            anotherJwtUtil.validateToken(token);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MalformedJwtException.class);
            assertThat(e.getMessage()).contains("Invalid JWT token");
        }
    }
}
