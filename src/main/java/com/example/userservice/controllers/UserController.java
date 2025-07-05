package com.example.userservice.controllers;

import com.example.userservice.dtos.*;
import com.example.userservice.dtos.ResponseStatus;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    //sign-up api implementation
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDTO> signupUser(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO)  {
        String username=signUpRequestDTO.getName();
        String email=signUpRequestDTO.getEmail();
        String password=signUpRequestDTO.getPassword();
        SignUpResponseDTO response = new SignUpResponseDTO();
        try {
            User user = userService.signupUser(username, email, password);
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setMessage("User signed up successfully");
            response.setStatus(ResponseStatus.SUCCESS);
        } catch (Exception e) {
            response.setMessage("Error signing up user: "+e.getMessage());
            response.setStatus(ResponseStatus.FAILURE);
        }

        return ResponseEntity.ok(response);
    }

    //login api implementation
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        String username = loginRequestDTO.getUsername();
        String password = loginRequestDTO.getPassword();
        LoginResponseDTO response = new LoginResponseDTO();

        try{
            String token = userService.login(username, password);
            User user = userService.getUserByUsername(username);
            response.setToken(token);
            response.setUsername(user.getName());
            response.setEmail(user.getEmail());
            response.setMessage("User logged in successfully");
            response.setStatus(ResponseStatus.SUCCESS);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error logging in: "+e.getMessage());
            response.setStatus(ResponseStatus.FAILURE);
            return ResponseEntity.badRequest().body(response);
        }
    }

    //logout api implementation
    @PatchMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequestDTO logoutRequestDTO){
        try{
            userService.logout(logoutRequestDTO.getToken());
            return ResponseEntity.ok("Logout successful");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Logout failed: "+e.getMessage());
        }
    }

    //profile api implementation (protected endpoint)
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Extract token from Authorization header
            String token = authHeader.substring(7); // Remove "Bearer " prefix

            // Get security claims from token
            Map<String, Object> securityClaims = userService.getSecurityClaimsFromToken(token);

            if (securityClaims.isEmpty()) {
                response.put("message", "Invalid token");
                response.put("status", ResponseStatus.FAILURE);
                return ResponseEntity.status(401).body(response);
            }

            response.put("userInfo", securityClaims);
            response.put("message", "Profile retrieved successfully");
            response.put("status", ResponseStatus.SUCCESS);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error retrieving profile: " + e.getMessage());
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.status(401).body(response);
        }
    }

    //logout all devices api implementation
    @PatchMapping("/logout-all")
    public ResponseEntity<Map<String, Object>> logoutAllDevices(
            @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> response = new HashMap<>();

        try {
            String token = authHeader.substring(7);
            int loggedOutTokens = userService.logoutAllDevices(token);

            response.put("message", "Logged out from " + loggedOutTokens + " devices");
            response.put("status", ResponseStatus.SUCCESS);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error logging out: " + e.getMessage());
            response.put("status", ResponseStatus.FAILURE);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
