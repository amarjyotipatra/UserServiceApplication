package com.example.userservice.controllers;

import com.example.userservice.dtos.LoginRequestDTO;
import com.example.userservice.dtos.SignUpRequestDTO;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSignupUser() {
        SignUpRequestDTO signUpRequestDTO = new SignUpRequestDTO();
        signUpRequestDTO.setName("testuser");
        signUpRequestDTO.setEmail("testuser@example.com");
        signUpRequestDTO.setPassword("password");

        User user = new User();
        user.setName("testuser");
        user.setEmail("testuser@example.com");

        when(userService.signupUser(anyString(), anyString(), anyString())).thenReturn(user);

        ResponseEntity<?> responseEntity = userController.signupUser(signUpRequestDTO);

        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testLogin() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");

        User user = new User();
        user.setName("testuser");
        user.setEmail("testuser@example.com");

        when(userService.login(anyString(), anyString())).thenReturn("test_token");
        when(userService.getUserByUsername(anyString())).thenReturn(user);

        ResponseEntity<?> responseEntity = userController.login(loginRequestDTO);

        assertEquals(200, responseEntity.getStatusCodeValue());
    }
}

