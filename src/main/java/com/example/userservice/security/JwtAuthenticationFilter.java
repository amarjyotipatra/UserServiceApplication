package com.example.userservice.security;

import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.services.JwtService;
import com.example.userservice.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Simplified JWT Authentication Filter without role-based authorization
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Skip JWT validation for public endpoints
        String requestPath = request.getRequestURI();
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);

        try {
            // Extract username from JWT
            username = jwtService.extractUsername(jwt);

            // If username is present and no authentication is set in security context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Verify token exists in database and is not deleted/expired
                boolean tokenExistsInDb = tokenRepository
                    .findByTokenAndIsDeletedFalseAndIsExpiredFalse(jwt)
                    .isPresent();

                if (!tokenExistsInDb) {
                    // Token doesn't exist in DB or is deleted/expired
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Token is invalid or expired\"}");
                    response.setContentType("application/json");
                    return;
                }

                // Load user details without role complexity
                UserDetails userDetails = loadUserDetails(username);

                // Validate JWT token
                if (jwtService.validateToken(jwt, username)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Define public endpoints that don't require authentication
     */
    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.equals("/api/v1/users/signup") ||
               requestPath.equals("/api/v1/users/login") ||
               requestPath.equals("/error") ||
               requestPath.startsWith("/actuator/") ||
               requestPath.startsWith("/api/v1/demo/") ||
               requestPath.startsWith("/api/v1/auth/");
    }

    /**
     * Simplified user details loading without role complexity
     */
    private UserDetails loadUserDetails(String username) {
        var user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Simple authority - just give basic USER authority to all authenticated users
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getName())
            .password(user.getPassword())
            .authorities(authorities)
            .build();
    }
}
