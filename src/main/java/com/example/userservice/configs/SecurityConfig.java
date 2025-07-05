package com.example.userservice.configs;

import com.example.userservice.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints - no authentication required
                .requestMatchers("/api/v1/users/signup", "/api/v1/users/login").permitAll()
                .requestMatchers("/actuator/**", "/error").permitAll()

                // Demo endpoints - public for testing
                .requestMatchers("/api/v1/demo/**").permitAll()

                // Token validation endpoints - public for microservice communication
                .requestMatchers("/api/v1/auth/**").permitAll()

                // Protected endpoints - authentication required
                .requestMatchers("/api/v1/users/logout").authenticated()
                .requestMatchers("/api/v1/users/profile").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions for JWT
            )
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable()) // Disabled for stateless JWT authentication

            // Add JWT filter before the standard authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
