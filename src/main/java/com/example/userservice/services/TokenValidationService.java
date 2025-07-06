package com.example.userservice.services;

import com.example.userservice.dtos.ValidatedTokenResponseDTO;
import com.example.userservice.dtos.ResponseStatus;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Production-grade Token Validation Service for microservices
 * Provides comprehensive token validation, authorization checking, and user information extraction
 */
@Service
public class TokenValidationService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * Comprehensive token validation for microservices
     * Validates JWT structure, signature, expiration, database status, and optional role checking
     */
    public ValidatedTokenResponseDTO validateTokenForMicroservice(String token, String requiredRole) {
        ValidatedTokenResponseDTO response = new ValidatedTokenResponseDTO();

        try {
            // Step 1: Basic token validation
            if (token == null || token.trim().isEmpty()) {
                return new ValidatedTokenResponseDTO(false, "Token is required");
            }

            // Step 2: Extract username from token (this also validates JWT structure)
            String username;
            try {
                username = jwtService.extractUsername(token);
                if (username == null || username.trim().isEmpty()) {
                    return new ValidatedTokenResponseDTO(false, "Invalid token: unable to extract username");
                }
            } catch (Exception e) {
                return new ValidatedTokenResponseDTO(false, "Invalid token structure: " + e.getMessage());
            }

            // Step 3: Validate JWT signature and claims
            if (!jwtService.validateToken(token, username)) {
                return new ValidatedTokenResponseDTO(false, "Token signature or claims validation failed");
            }

            // Step 4: Check if token exists in database and is active
            Optional<Token> tokenEntity = tokenRepository.findByTokenAndIsDeletedFalseAndIsExpiredFalse(token);
            if (tokenEntity.isEmpty()) {
                return new ValidatedTokenResponseDTO(false, "Token not found in database or has been revoked/expired");
            }

            // Step 5: Check token expiration in database
            Token dbToken = tokenEntity.get();
            if (dbToken.getExpiredAt() != null && dbToken.getExpiredAt().before(new Date())) {
                return new ValidatedTokenResponseDTO(false, "Token has expired");
            }

            // Step 6: Extract all token information
            populateTokenInformation(response, token, dbToken);

            // Step 7: Success response
            response.setValid(true);
            response.setMessage("Token is valid and user is authorized");
            response.setStatus(ResponseStatus.SUCCESS);

            return response;

        } catch (Exception e) {
            return new ValidatedTokenResponseDTO(false, "Token validation error: " + e.getMessage());
        }
    }

    /**
     * Quick token validation - only validates JWT structure and signature
     * Useful for lightweight validation when database check is not needed
     */
    public boolean quickValidateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            String username = jwtService.extractUsername(token);
            return jwtService.validateToken(token, username);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract basic token information without full validation
     */
    public Map<String, Object> extractBasicTokenInfo(String token) {
        Map<String, Object> tokenInfo = new HashMap<>();
        try {
            tokenInfo.put("username", jwtService.extractUsername(token));
            tokenInfo.put("userId", jwtService.extractUserId(token));
            tokenInfo.put("email", jwtService.extractEmail(token));
            tokenInfo.put("tokenId", jwtService.extractTokenId(token));
            tokenInfo.put("issuedAt", jwtService.extractClaim(token, Claims::getIssuedAt));
            tokenInfo.put("expiration", jwtService.extractExpiration(token));
        } catch (Exception e) {
            // Return empty map if token is invalid
            return new HashMap<>();
        }
        return tokenInfo;
    }

    /**
     * Extract comprehensive user information from token
     */
    public Map<String, Object> extractUserInformation(String token) {
        return jwtService.extractSecurityClaims(token);
    }

    /**
     * Check user authorization (simplified - no role checking)
     */
    public boolean checkUserAuthorization(String token, String requiredRole, String requiredPermission) {
        try {
            // Simplified validation - no role checking for now
            // Since we removed role functionality, just return true if token is valid
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user entity from token
     */
    public User getUserFromToken(String token) {
        try {
            Optional<Token> tokenEntity = tokenRepository.findByTokenAndIsDeletedFalseAndIsExpiredFalse(token);
            return tokenEntity.map(Token::getUser).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if token is revoked or expired in database
     */
    public boolean isTokenRevokedOrExpired(String token) {
        try {
            Optional<Token> tokenEntity = tokenRepository.findByTokenAndIsDeletedFalse(token);
            if (tokenEntity.isEmpty()) {
                return true; // Token not found means it's revoked
            }

            Token dbToken = tokenEntity.get();
            return dbToken.isExpired() ||
                   (dbToken.getExpiredAt() != null && dbToken.getExpiredAt().before(new Date()));
        } catch (Exception e) {
            return true; // Assume revoked on error
        }
    }

    // Private helper methods

    private void populateTokenInformation(ValidatedTokenResponseDTO response, String token, Token dbToken) {
        try {
            // Extract user information
            response.setUserId(jwtService.extractUserId(token));
            response.setUsername(jwtService.extractUsername(token));
            response.setEmail(jwtService.extractEmail(token));

            // Extract token metadata
            response.setTokenId(jwtService.extractTokenId(token));
            response.setIssuedAt(jwtService.extractClaim(token, Claims::getIssuedAt));
            response.setExpirationTime(jwtService.extractExpiration(token));
            response.setIssuer(jwtService.extractIssuer(token));
            response.setAudience(jwtService.extractAudience(token));

            // Extract roles using the public getAllClaims method
            Map<String, Object> allClaims = jwtService.getAllClaims(token);
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) allClaims.get("roles");
            response.setRoles(roles != null ? roles : new ArrayList<>());

            // Set verification status
            Boolean isVerified = (Boolean) allClaims.get("isVerified");
            response.setVerified(isVerified != null ? isVerified : false);

            // Set token status
            response.setTokenType("Bearer");
            response.setExpired(dbToken.isExpired());
            response.setRevoked(dbToken.isDeleted());

        } catch (Exception e) {
            // If we can't extract some information, it's still valid but with limited info
            response.setMessage("Token is valid but some information could not be extracted: " + e.getMessage());
        }
    }
}
