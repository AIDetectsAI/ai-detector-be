package org.example.aidetectorbe.security;

import org.springframework.stereotype.Component;
import org.example.aidetectorbe.logger.Log;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request, 
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String errorMessage = authException != null ? authException.getMessage() : "Authentication required";
        if (errorMessage != null) {
            errorMessage = errorMessage.replace("\"", "\\\"");
        }
        Log.error("Unauthorized error: " + errorMessage);
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = String.format(
            "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"status\": %d}",
            errorMessage,
            HttpServletResponse.SC_UNAUTHORIZED
        );
        
        response.getWriter().write(jsonResponse);
    }
}