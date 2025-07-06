package com.example.userservice.services;

import com.example.userservice.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "a-string-secret-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "issuer", "user-service");
        ReflectionTestUtils.setField(jwtService, "audience", "user-service-clients");
    }

    @Test
    public void testGenerateToken() {
        User user = new User();
        user.setId(1L);
        user.setName("testuser");
        user.setEmail("testuser@example.com");

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
    }
}

