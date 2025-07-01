package com.example.userservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpResponseDTO {
    private String name;
    private String email;

    public SignUpResponseDTO(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
