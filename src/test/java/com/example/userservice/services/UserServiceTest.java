package com.example.userservice.services;

import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private SCryptPasswordEncoder sCryptPasswordEncoder;

    @Mock
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSignupUser() {
        User user = new User();
        user.setName("testuser");
        user.setEmail("testuser@example.com");

        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(sCryptPasswordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signupUser("testuser", "testuser@example.com", "password");

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals("testuser", savedUser.getName());
        assertEquals("testuser@example.com", savedUser.getEmail());
        assertEquals("encoded_password", savedUser.getPassword());
    }

    @Test
    public void testLogin() {
        User user = new User();
        user.setName("testuser");
        user.setPassword("encoded_password");

        when(userRepository.findByName(anyString())).thenReturn(user);
        when(sCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("test_token");

        String token = userService.login("testuser", "password");

        assertEquals("test_token", token);
    }
}
