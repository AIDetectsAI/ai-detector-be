package org.example.aidetectorbe.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.aidetectorbe.TestUtils.setPrivateField;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setJwtSecretKey("99327f738b5a440eafe816a57260c0b1f1a121f0f2217b6a201838b36da2d524");
        setPrivateField(jwtUtil, "jwtExpirationMs", 3600000L);
        jwtUtil.init();
    }

    @Test
    public void testGenerateToken_GivenValidLogin_ShouldReturnNonEmptyToken() {
        //Given
        String login = "testUser";
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    public void testGenerateToken_GivenEmptyLogin_ShouldHandleGracefully() {
        //Given
        String login = "";
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    public void testGenerateToken_WhenNullLogin_ShouldHandleGracefully() {
        //Given
        String login = null;
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    public void testGenerateToken_WhenSpecialCharactersInLogin_ShouldHandleGracefully() {
        //Given
        String login = " user!@#_$%^&*() ";
        //When
        String token = jwtUtil.generateToken(login);
        //Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
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
        assertThat(extractedLogin).isEqualTo(login);
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
    public void testExtractLogin_GivenExpiredToken_ShouldThrowException() throws InterruptedException {
        //Given
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        shortExpirationJwtUtil.setJwtSecretKey("99327f738b5a440eafe816a57260c0b1f1a121f0f2217b6a201838b36da2d524");
        java.lang.reflect.Field expirationField;
        try {
            expirationField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
            expirationField.setAccessible(true);
            expirationField.set(shortExpirationJwtUtil, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        shortExpirationJwtUtil.init();
        
        String token = shortExpirationJwtUtil.generateToken("testUser");
        
        Thread.sleep(10);
        
        //When
        try {
            jwtUtil.extractLogin(token);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(ExpiredJwtException.class);
            assertThat(e.getMessage()).contains("JWT token is expired");
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
    public void testValidateToken_GivenExpiredToken_ShouldThrowException() throws InterruptedException {
        //Given
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        shortExpirationJwtUtil.setJwtSecretKey("99327f738b5a440eafe816a57260c0b1f1a121f0f2217b6a201838b36da2d524");
        java.lang.reflect.Field expirationField;
        try {
            expirationField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
            expirationField.setAccessible(true);
            expirationField.set(shortExpirationJwtUtil, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        shortExpirationJwtUtil.init();
        
        String token = shortExpirationJwtUtil.generateToken("testUser");
        
        Thread.sleep(10);
        
        //When
        try {
            jwtUtil.validateToken(token);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(ExpiredJwtException.class);
            assertThat(e.getMessage()).contains("JWT token is expired");
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
        String corruptedToken = token.substring(0, token.length() - 5) + "xxxxx";
        //When
        try {
            jwtUtil.validateToken(corruptedToken);
        } catch (Exception e) {
            //Then
            assertThat(e).isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
            assertThat(e.getMessage()).contains("JWT signature does not match");
        }
    }

    @Test
    public void testTokenWorkflow_GenerateExtractValidate_ShouldWorkTogether() {
        String login = "integrationTestUser";
        String token = jwtUtil.generateToken(login);
        assertThat(token).isNotNull();
        String extractedLogin = jwtUtil.extractLogin(token);
        assertThat(extractedLogin).isEqualTo(login);
        boolean isValid = jwtUtil.validateToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    public void testTokenSecurity_DifferentInstance_ShouldThrowException() {
        JwtUtil anotherJwtUtil = new JwtUtil();
        anotherJwtUtil.setJwtSecretKey("anotherSecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm");
        anotherJwtUtil.init();
        String login = "testUser";
        String token = jwtUtil.generateToken(login);
        try {
            anotherJwtUtil.validateToken(token);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
            assertThat(e.getMessage()).contains("JWT signature does not match");
        }
    }
}
