package com.example.userservice.services;

import com.example.userservice.exceptions.UserAlreadyExistsException;
import com.example.userservice.models.User;
import com.example.userservice.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository=userRepository;
        this.bCryptPasswordEncoder=bCryptPasswordEncoder;
    }

    public User createUser(String username, String email, String password) {
        if (userRepository.existsByName(username)) {
            throw new UserAlreadyExistsException("Username '" + username + "' already exists. Please choose a different username.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email '" + email + "' is already registered. Please use a different email address.");
        }

        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        User user = new User();
        user.setName(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(bCryptPasswordEncoder.encode(password));

        return userRepository.save(user);
    }
//
//    public Token login(String username, String password){
//        User user=userRepository.findByName(username);
//        if(user==null) throw new IllegalArgumentException("Username not found");
//        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) throw new IllegalArgumentException("Invalid password");
//
//    }

}
