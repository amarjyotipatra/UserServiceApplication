package com.example.userservice.controllers;

import com.example.userservice.dtos.ValidateTokenRequestDTO;
import com.example.userservice.dtos.ValidatedTokenResponseDTO;
import com.example.userservice.services.TokenValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TokenValidationControllerTest {

    @InjectMocks
    private TokenValidationController tokenValidationController;

    @Mock
    private TokenValidationService tokenValidationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testValidateToken() {
        ValidateTokenRequestDTO request = new ValidateTokenRequestDTO();
        request.setToken("test_token");
        request.setRequiredRole("USER");

        ValidatedTokenResponseDTO response = new ValidatedTokenResponseDTO(true, "Token is valid");

        when(tokenValidationService.validateTokenForMicroservice(anyString(), anyString())).thenReturn(response);

        ResponseEntity<ValidatedTokenResponseDTO> responseEntity = tokenValidationController.validateToken(request);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(true, responseEntity.getBody().isValid());
    }
}

