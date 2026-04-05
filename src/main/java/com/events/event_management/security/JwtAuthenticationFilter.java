  package com.events.event_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // Extract email and role from token
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                
                // Validate token
                if (email != null && !jwtUtil.isTokenExpired(token)) {
                    // Create authentication
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            email, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                        );
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("JWT Authentication successful for user: " + email + " with role: " + role);
                }
            }
        } catch (Exception e) {
            logger.error("JWT Authentication failed: " + e.getMessage());
        }
        
        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}