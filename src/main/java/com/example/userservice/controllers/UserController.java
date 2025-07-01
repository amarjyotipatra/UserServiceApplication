package com.example.userservice.controllers;

import com.example.userservice.dtos.SignUpRequestDTO;
import com.example.userservice.dtos.SignUpResponseDTO;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    //sign-up api implementation
    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponseDTO> createUser(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        String username=signUpRequestDTO.getName();
        String email=signUpRequestDTO.getEmail();
        String password=signUpRequestDTO.getPassword();

        User user = userService.createUser(username, email, password);
        SignUpResponseDTO response = new SignUpResponseDTO(user.getName(), user.getEmail());
        return ResponseEntity.ok(response);
    }
}
