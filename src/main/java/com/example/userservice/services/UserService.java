package com.example.userservice.services;

import com.example.userservice.exceptions.InvalidCredentialsException;
import com.example.userservice.exceptions.UserAlreadyExistsException;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final SCryptPasswordEncoder sCryptPasswordEncoder;
    private final JwtService jwtService;

    @Autowired
    private TokenService tokenService;

    public UserService(UserRepository userRepository, TokenRepository tokenRepository,
                      SCryptPasswordEncoder sCryptPasswordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.sCryptPasswordEncoder = sCryptPasswordEncoder;
        this.jwtService = jwtService;
    }

    public User signupUser(String username, String email, String password) {
        if (userRepository.existsByName(username)) {
            throw new UserAlreadyExistsException("Username '" + username + "' already exists. Please choose a different username.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email '" + email + "' is already registered. Please use a different email address.");
        }

        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        User user = new User();
        user.setName(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(sCryptPasswordEncoder.encode(password));
        user.setVerified(false); // Set default verification status

        return userRepository.save(user);
    }

    public String login(String username, String password) {
        User user = userRepository.findByName(username);
        if (user == null) {
            throw new InvalidCredentialsException("Invalid username");
        }
        if (!sCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        // Save token to database for tracking
        Token token = new Token();
        token.setToken(jwtToken);
        token.setUser(user);
        token.setExpired(false);

        // Set expiration date (24 hours from now, matching JWT expiration)
        Date expirationDate = new Date(System.currentTimeMillis() + 86400000); // 24 hours
        token.setExpiredAt(expirationDate);

        // Actually save the token to database
        tokenRepository.save(token);

        return jwtToken;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByName(username);
    }

    public void logout(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be empty");
        }

        // Find the token in database
        Token token = tokenRepository.findByTokenAndIsDeletedFalse(tokenString.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or already logged out token"));

        // Mark token as deleted (soft delete)
        token.setDeleted(true);
        tokenRepository.save(token);
    }

    /**
     * Get security claims from token for user profile
     */
    public Map<String, Object> getSecurityClaimsFromToken(String token) {
        return jwtService.extractSecurityClaims(token);
    }
}
