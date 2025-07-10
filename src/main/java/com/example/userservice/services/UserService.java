package com.example.userservice.services;

import com.example.userservice.events.SendEmail;
import com.example.userservice.exceptions.InvalidCredentialsException;
import com.example.userservice.exceptions.UserAlreadyExistsException;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TokenService tokenService;

    public UserService(UserRepository userRepository, TokenRepository tokenRepository,
                      BCryptPasswordEncoder passwordEncoder, JwtService jwtService,
                       KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaTemplate = kafkaTemplate;
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
        user.setPassword(passwordEncoder.encode(password));
        user.setVerified(false); // Set default verification status
        //publish this user to kafka
        //email- from,to,subject,body
        SendEmail sendEmail = new SendEmail();
        sendEmail.setFrom("admin@userservice.com");
        sendEmail.setTo(email);
        sendEmail.setSubject("Welcome to User Service");
        sendEmail.setBody("Hello " + username + ",\n\nThank you for signing up! Please verify your email address to complete the registration process.\n\nBest regards,\nUser Service Team");
        kafkaTemplate.send("email-topic", sendEmail.toString());
        return userRepository.save(user);
    }

    public String login(String username, String password) {
        User user = userRepository.findByName(username);
        if (user == null) {
            throw new InvalidCredentialsException("Invalid username");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
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
