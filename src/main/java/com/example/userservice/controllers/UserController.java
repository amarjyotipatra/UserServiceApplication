package com.example.userservice.controllers;

import com.example.userservice.dtos.SignUpRequestDTO;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
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
    public ResponseEntity<User> createUser(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        String username=signUpRequestDTO.getName();
        String email=signUpRequestDTO.getEmail();
        String password=signUpRequestDTO.getPassword();

        return ResponseEntity.ok(userService.createUser(username, email, password));
    }
}
