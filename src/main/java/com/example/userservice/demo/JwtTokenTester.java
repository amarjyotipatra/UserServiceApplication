package com.example.userservice.demo;

import com.example.userservice.models.User;
import com.example.userservice.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

/**
 * Demo class to test JWT with the exact token you provided
 * Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30
 */
@Component
public class JwtTokenTester {

    @Autowired
    private JwtService jwtService;

    // Your demo token
    private static final String DEMO_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

    public void testDemoToken() {
        System.out.println("=== JWT DEMO TOKEN TESTING ===");
        System.out.println("Testing with your demo token...");
        System.out.println();

        // 1. Show the token structure
        showTokenStructure();

        // 2. Test token parsing and validation
        testTokenParsing();

        // 3. Generate a similar token using your service
        testTokenGeneration();

        // 4. Test the exact token reproduction
        testExactTokenReproduction();
    }

    private void showTokenStructure() {
        System.out.println("1. TOKEN STRUCTURE ANALYSIS:");
        System.out.println("Complete Token: " + DEMO_TOKEN);
        System.out.println();

        String[] parts = DEMO_TOKEN.split("\\.");
        System.out.println("Header (Part 1): " + parts[0]);
        System.out.println("Payload (Part 2): " + parts[1]);
        System.out.println("Signature (Part 3): " + parts[2]);
        System.out.println();

        // Decode header
        try {
            String decodedHeader = new String(Base64.getUrlDecoder().decode(parts[0]));
            System.out.println("Decoded Header: " + decodedHeader);
        } catch (Exception e) {
            System.out.println("Error decoding header: " + e.getMessage());
        }

        // Decode payload
        try {
            String decodedPayload = new String(Base64.getUrlDecoder().decode(parts[1]));
            System.out.println("Decoded Payload: " + decodedPayload);
        } catch (Exception e) {
            System.out.println("Error decoding payload: " + e.getMessage());
        }
        System.out.println();
    }

    private void testTokenParsing() {
        System.out.println("2. TOKEN PARSING TEST:");

        try {
            // Test extracting claims from the demo token
            String username = jwtService.extractUsername(DEMO_TOKEN);
            System.out.println("✓ Extracted Username: " + username);

            String email = jwtService.extractEmail(DEMO_TOKEN);
            System.out.println("✓ Extracted Email: " + email);

            Long userId = jwtService.extractUserId(DEMO_TOKEN);
            System.out.println("✓ Extracted User ID: " + userId);

            // Get all claims
            Map<String, Object> allClaims = jwtService.getAllClaims(DEMO_TOKEN);
            System.out.println("✓ All Claims: " + allClaims);

        } catch (Exception e) {
            System.out.println("✗ Error parsing token: " + e.getMessage());
            System.out.println("This is expected because the demo token uses a different secret key");
        }
        System.out.println();
    }

    private void testTokenGeneration() {
        System.out.println("3. TOKEN GENERATION TEST:");

        // Create a user similar to the demo token
        User demoUser = createDemoUser();

        try {
            // Generate a token using our service
            String generatedToken = jwtService.generateToken(demoUser);
            System.out.println("✓ Generated Token: " + generatedToken);

            // Test the generated token
            String extractedUsername = jwtService.extractUsername(generatedToken);
            System.out.println("✓ Extracted Username from Generated Token: " + extractedUsername);

            // Validate the generated token
            boolean isValid = jwtService.validateToken(generatedToken, demoUser.getName());
            System.out.println("✓ Generated Token Valid: " + isValid);

        } catch (Exception e) {
            System.out.println("✗ Error generating token: " + e.getMessage());
        }
        System.out.println();
    }

    private void testExactTokenReproduction() {
        System.out.println("4. EXACT TOKEN REPRODUCTION TEST:");

        try {
            // Try to generate the exact same token as your demo
            String exactToken = jwtService.generateExactExampleToken();
            System.out.println("Generated Exact Token: " + exactToken);
            System.out.println("Demo Token:           " + DEMO_TOKEN);
            System.out.println("Tokens Match: " + exactToken.equals(DEMO_TOKEN));

            if (!exactToken.equals(DEMO_TOKEN)) {
                System.out.println("Note: Tokens don't match because:");
                System.out.println("1. Different secret keys might be used");
                System.out.println("2. Different JWT library implementations");
                System.out.println("3. Different header/payload ordering");
            }

        } catch (Exception e) {
            System.out.println("✗ Error reproducing exact token: " + e.getMessage());
        }
        System.out.println();
    }

    private User createDemoUser() {
        User user = new User();
        user.setId(1234567890L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setVerified(true);
        return user;
    }

    /**
     * Test the demo token with the correct secret key
     */
    public void testWithCorrectSecret() {
        System.out.println("5. TESTING WITH CORRECT SECRET:");

        // Note: Your application.properties has the secret: Kh4CpDhM8NzT7XjG5RqL3VbE9WyS1A6F
        // But the demo token was created with: a-string-secret-at-least-256-bits-long

        System.out.println("Current JWT Secret in app: Kh4CpDhM8NzT7XjG5RqL3VbE9WyS1A6F");
        System.out.println("Demo Token Secret needed: a-string-secret-at-least-256-bits-long");
        System.out.println();
        System.out.println("To test the demo token, you need to temporarily change the secret in application.properties");
        System.out.println("or create a separate test configuration.");
    }
}
