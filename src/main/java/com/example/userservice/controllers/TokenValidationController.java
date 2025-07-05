package com.example.userservice.controllers;

import com.example.userservice.dtos.ValidateTokenRequestDTO;
import com.example.userservice.dtos.ValidatedTokenResponseDTO;
import com.example.userservice.dtos.ResponseStatus;
import com.example.userservice.services.TokenValidationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Production-grade Token Validation API for inter-microservice communication
 * This controller provides secure token validation endpoints for other microservices
 */
@RestController
@RequestMapping("/api/v1/auth")
public class TokenValidationController {

    @Autowired
    private TokenValidationService tokenValidationService;

    /**
     * Comprehensive token validation endpoint for microservices
     * Validates JWT structure, signature, expiration, and database status
     */
    @PostMapping("/validate-token")
    public ResponseEntity<ValidatedTokenResponseDTO> validateToken(@Valid @RequestBody ValidateTokenRequestDTO request) {

        try {
            ValidatedTokenResponseDTO response = tokenValidationService
                .validateTokenForMicroservice(request.getToken(), request.getRequiredRole());

            if (response.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            ValidatedTokenResponseDTO errorResponse = new ValidatedTokenResponseDTO(
                false,
                "Token validation failed: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Quick token validation endpoint (lightweight)
     * Only validates JWT structure and signature
     */
    @PostMapping("/quick-validate")
    public ResponseEntity<Map<String, Object>> quickValidateToken(
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");

        if (token == null || token.trim().isEmpty()) {
            response.put("valid", false);
            response.put("message", "Token is required");
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean isValid = tokenValidationService.quickValidateToken(token);

            response.put("valid", isValid);
            response.put("message", isValid ? "Token is valid" : "Token is invalid");
            response.put("status", isValid ? ResponseStatus.SUCCESS : ResponseStatus.FAILURE);

            if (isValid) {
                // Add basic token info for quick validation
                Map<String, Object> tokenInfo = tokenValidationService.extractBasicTokenInfo(token);
                response.put("tokenInfo", tokenInfo);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token validation error: " + e.getMessage());
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Extract user information from token without full validation
     * Useful for internal microservice communication where token is already validated
     */
    @PostMapping("/extract-user")
    public ResponseEntity<Map<String, Object>> extractUserFromToken(
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");

        if (token == null || token.trim().isEmpty()) {
            response.put("message", "Token is required");
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> userInfo = tokenValidationService.extractUserInformation(token);

            if (userInfo.isEmpty()) {
                response.put("message", "Unable to extract user information from token");
                response.put("status", ResponseStatus.FAILURE);
                return ResponseEntity.status(401).body(response);
            }

            response.put("userInfo", userInfo);
            response.put("message", "User information extracted successfully");
            response.put("status", ResponseStatus.SUCCESS);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error extracting user information: " + e.getMessage());
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Check if user has specific role or permission
     * For authorization decisions in other microservices
     */
    @PostMapping("/check-authorization")
    public ResponseEntity<Map<String, Object>> checkAuthorization(
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");
        String requiredRole = request.get("role");
        String requiredPermission = request.get("permission");

        if (token == null || token.trim().isEmpty()) {
            response.put("authorized", false);
            response.put("message", "Token is required");
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean isAuthorized = tokenValidationService.checkUserAuthorization(
                token, requiredRole, requiredPermission);

            response.put("authorized", isAuthorized);
            response.put("message", isAuthorized ? "User is authorized" : "User is not authorized");
            response.put("status", ResponseStatus.SUCCESS);

            // Add authorization details
            Map<String, Object> authDetails = new HashMap<>();
            authDetails.put("hasRole", requiredRole != null &&
                tokenValidationService.hasRole(token, requiredRole));
            authDetails.put("checkedRole", requiredRole);
            authDetails.put("checkedPermission", requiredPermission);
            response.put("authorizationDetails", authDetails);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("authorized", false);
            response.put("message", "Authorization check failed: " + e.getMessage());
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Health check endpoint for the token validation service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Token Validation Service");
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
