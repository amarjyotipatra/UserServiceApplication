package com.example.userservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateTokenRequestDTO {
    private String token;
    private String requiredRole; // Optional: for role-based validation
    private String requiredPermission; // Optional: for permission-based validation
}
