package com.example.userservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ValidatedTokenResponseDTO {
    private boolean valid;
    private String message;
    private ResponseStatus status;

    // User information from token
    private Long userId;
    private String username;
    private String email;
    private boolean isVerified;

    // Token metadata
    private String tokenId;
    private Date issuedAt;
    private Date expirationTime;
    private String issuer;
    private String audience;

    // Authorization information
    private List<String> roles;
    private List<String> permissions;

    // Additional security context
    private String tokenType;
    private boolean isExpired;
    private boolean isRevoked;

    public ValidatedTokenResponseDTO() {
        this.valid = false;
        this.status = ResponseStatus.FAILURE;
    }

    public ValidatedTokenResponseDTO(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
        this.status = valid ? ResponseStatus.SUCCESS : ResponseStatus.FAILURE;
    }
}
