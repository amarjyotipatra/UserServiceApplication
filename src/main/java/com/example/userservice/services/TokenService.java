package com.example.userservice.services;

import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Production-grade token management service
 * Handles token validation, cleanup, and security operations
 */
@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Validates if a token is active and valid
     */
    public boolean isTokenValid(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return false;
        }

        // Check if token exists in database and is active
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndIsDeletedFalseAndIsExpiredFalse(tokenString);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        Token token = tokenOpt.get();

        // Check if token has expired based on expiredAt field
        if (token.getExpiredAt() != null && token.getExpiredAt().before(new Date())) {
            // Mark token as expired in database
            token.setExpired(true);
            tokenRepository.save(token);
            return false;
        }

        // Validate JWT structure and signature
        try {
            String username = jwtService.extractUsername(tokenString);
            return jwtService.validateToken(tokenString, username);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user from token
     */
    public User getUserFromToken(String tokenString) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndIsDeletedFalseAndIsExpiredFalse(tokenString);
        return tokenOpt.map(Token::getUser).orElse(null);
    }

    /**
     * Logout user from specific token
     */
    @Transactional
    public boolean logoutToken(String tokenString) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndIsDeletedFalse(tokenString);
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            token.setDeleted(true);
            tokenRepository.save(token);
            return true;
        }
        return false;
    }

    /**
     * Logout user from all devices (invalidate all tokens)
     */
    @Transactional
    public int logoutAllUserTokens(User user) {
        return tokenRepository.markAllUserTokensAsDeleted(user);
    }

    /**
     * Get all active tokens for a user
     */
    public List<Token> getActiveTokensForUser(User user) {
        return tokenRepository.findByUserAndIsDeletedFalseAndIsExpiredFalse(user);
    }

    /**
     * Scheduled task to clean up expired tokens
     * Runs every hour to mark expired tokens
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        int markedExpired = tokenRepository.markExpiredTokens();
        if (markedExpired > 0) {
            System.out.println("Marked " + markedExpired + " tokens as expired");
        }
    }

    /**
     * Revoke token (for admin use)
     */
    @Transactional
    public boolean revokeToken(String tokenString, String reason) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndIsDeletedFalse(tokenString);
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            token.setDeleted(true);
            // You could add a reason field to the Token model if needed
            tokenRepository.save(token);
            return true;
        }
        return false;
    }
}
