package org.example.aidetectorbe.security;

import org.springframework.stereotype.Component;
import org.example.aidetectorbe.dto.ErrorResponse;
import org.example.aidetectorbe.logger.Log;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void commence(
            HttpServletRequest request, 
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String errorMessage = authException != null ? authException.getMessage() : "Authentication required";
        Log.error("Unauthorized error: " + errorMessage);
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ErrorResponse errorResponse = new ErrorResponse("Unauthorized", errorMessage, 401);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        
        response.getWriter().write(jsonResponse);
    }
}