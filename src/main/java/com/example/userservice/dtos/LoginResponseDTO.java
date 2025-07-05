package com.example.userservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {
    private String token;
    private String username;
    private String email;
    private String tokenType = "Bearer";
    private  String message;
    private ResponseStatus status;
}
