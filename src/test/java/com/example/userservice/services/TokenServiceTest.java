package com.example.userservice.services;

import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIsTokenValid() {
        Token token = new Token();
        token.setToken("test_token");
        token.setExpired(false);
        token.setExpiredAt(new Date(System.currentTimeMillis() + 3600000));

        when(tokenRepository.findByTokenAndIsDeletedFalseAndIsExpiredFalse(anyString())).thenReturn(Optional.of(token));
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(jwtService.validateToken(anyString(), anyString())).thenReturn(true);

        boolean isValid = tokenService.isTokenValid("test_token");

        assertTrue(isValid);
    }

    @Test
    public void testIsTokenValid_whenTokenIsInvalid() {
        when(tokenRepository.findByTokenAndIsDeletedFalseAndIsExpiredFalse(anyString())).thenReturn(Optional.empty());

        boolean isValid = tokenService.isTokenValid("invalid_token");

        assertFalse(isValid);
    }
}

