package com.example.userservice.services;

import com.example.userservice.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret:a-string-secret-at-least-256-bits-long}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long jwtExpiration;

    @Value("${jwt.issuer:user-service}")
    private String issuer;

    @Value("${jwt.audience:user-service-clients}")
    private String audience;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generates a comprehensive JWT token with standard and custom claims.
     * The token will have 3 parts: Header.Payload.Signature
     *
     * Header: Contains algorithm and token type.
     * Payload: Contains standard claims (iss, sub, aud, exp, iat, jti) + custom claims.
     * Signature: HMAC SHA256 signature using the configured secret key.
     */
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + jwtExpiration);

        // Custom claims (application-specific data)
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("username", user.getName());
        claims.put("isVerified", user.isVerified());

        // Add roles if they exist
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            claims.put("roles", user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(Collectors.toList()));
        }

        return Jwts.builder()
                // Standard Claims
                .issuer(issuer)                           // iss: who issued the token
                .subject(user.getName())                  // sub: subject (username)
                .audience().add(audience).and()           // aud: intended audience
                .issuedAt(now)                            // iat: issued at time
                .expiration(expirationTime)               // exp: expiration time
                .id(UUID.randomUUID().toString())         // jti: unique token ID

                // Custom Claims
                .claims(claims)

                // Signature
                .signWith(getSigningKey())                // Sign with the correct key
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractAudience(String token) {
        return extractClaim(token, claims ->
                claims.getAudience() != null && !claims.getAudience().isEmpty()
                        ? claims.getAudience().iterator().next()
                        : null
        );
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            return userId instanceof Number ? ((Number) userId).longValue() : null;
        });
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> (String) claims.get("email"));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // This parser is only for extracting claims, not for validation.
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * BUG FIX: Rewritten for efficiency and security.
     * Validates the token's signature, expiration, issuer, audience, and subject in a single pass.
     * This is the recommended approach as it's more robust and performant.
     *
     * @param token The JWT token string.
     * @param username The username we expect to be the token's subject.
     * @return true if the token is valid for the given user, false otherwise.
     */
    public Boolean validateToken(String token, String username) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSigningKey())        // 1. Verifies signature
                    .requireIssuer(issuer)              // 2. Verifies 'iss' claim
                    .requireAudience(audience)          // 3. Verifies 'aud' claim
                    .requireSubject(username)           // 4. Verifies 'sub' claim
                    .build();

            // The parser automatically checks the 'exp' (expiration) claim.
            // If any of the checks fail, a JwtException is thrown.
            parser.parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Catches all JWT-related exceptions: expired, malformed, incorrect signature, etc.
            // It's good practice to log this for debugging purposes.
            // log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Enhanced token validation for production use
     * Checks JWT signature, expiration, and database token status
     */
    public Boolean validateTokenWithDatabase(String token, String username, TokenService tokenService) {
        try {
            // First validate JWT structure and signature
            if (!validateToken(token, username)) {
                return false;
            }

            // Then check if token exists in database and is active
            return tokenService.isTokenValid(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract all security-relevant claims for authorization
     */
    public Map<String, Object> extractSecurityClaims(String token) {
        Map<String, Object> securityClaims = new HashMap<>();
        try {
            securityClaims.put("userId", extractUserId(token));
            securityClaims.put("username", extractUsername(token));
            securityClaims.put("email", extractEmail(token));
            securityClaims.put("issuedAt", extractClaim(token, Claims::getIssuedAt));
            securityClaims.put("expiration", extractExpiration(token));
            securityClaims.put("tokenId", extractTokenId(token));

            // Extract roles for authorization
            Claims claims = extractAllClaims(token);
            Object roles = claims.get("roles");
            if (roles != null) {
                securityClaims.put("roles", roles);
            }
        } catch (Exception e) {
            // Return empty map if token is invalid
            return new HashMap<>();
        }
        return securityClaims;
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String token, String role) {
        try {
            Claims claims = extractAllClaims(token);
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            return roles != null && roles.contains(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get all claims from token for debugging/logging.
     */
    public Map<String, Object> getAllClaims(String token) {
        return new HashMap<>(extractAllClaims(token));
    }

    /**
     * Generates the exact JWT token from your example
     * This will create: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30
     */
    public String generateExactExampleToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "1234567890");
        claims.put("name", "John Doe");
        claims.put("admin", true);
        claims.put("iat", 1516239022);

        return Jwts.builder()
                .claims(claims)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates a simple JWT token with the exact format you specified
     * Header: {"alg": "HS256", "typ": "JWT"}
     * Payload: {"sub": "userId", "name": "username", "admin": isAdmin, "iat": currentTime}
     */
    public String generateSimpleToken(User user, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("name", user.getName());
        claims.put("admin", isAdmin);

        // Use current timestamp for iat (issued at)
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        claims.put("iat", currentTimeSeconds);

        return Jwts.builder()
                .claims(claims)
                .signWith(getSigningKey())
                .compact();
    }
}