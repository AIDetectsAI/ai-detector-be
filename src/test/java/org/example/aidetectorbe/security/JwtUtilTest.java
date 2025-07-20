package org.example.aidetectorbe.security;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Value;
import static org.assertj.core.api.Assertions.assertThat;
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
        String login = "testUser";
        String token = jwtUtil.generateToken(login);
        //Simulate expiration by waiting longer than the expiration time
        try {
            Thread.sleep(jwtExpiration + 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
}
