package org.example.aidetectorbe.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.example.aidetectorbe.logger.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecretKey;
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }
    //Token generation
    public String generateToken(String login) {
        Log.info("Generating JWT token for login: " + login);
        return Jwts.builder()
                .setSubject(login)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractLogin(String token)  throws JwtException {
        try {
            Log.info("Extracting login from JWT token");
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            Log.error("JWT token is expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            Log.error("Invalid JWT token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.error("JWT claims string is empty: " + e.getMessage());
        }
        return null;
    }

    public boolean validateToken(String token) throws JwtException {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            Log.info("Validated JWT token successfully");
            return true;
        } catch (SecurityException e) {
            Log.error("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            Log.error("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            Log.error("JWT token is expired: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.error("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    public void setJwtSecretKey(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
        this.secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        Log.info("JWT secret key has been set");
    }
}