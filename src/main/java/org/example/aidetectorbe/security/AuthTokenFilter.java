package org.example.aidetectorbe.security;

import org.example.aidetectorbe.logger.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                String login = jwtUtil.extractLogin(jwt);
                request.setAttribute("login", login);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            Log.error("JWT authentication failed: " + e.getMessage());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Authentication failed";
            String jsonResponse = String.format(
                "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"status\": %d}",
                errorMessage,
                HttpServletResponse.SC_UNAUTHORIZED
            );
            response.getWriter().write(jsonResponse);
        }
    }

    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
    
}
