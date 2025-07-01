package com.example.userservice.repositories;


import com.example.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);
    boolean existsByName(String name);
    boolean existsByEmail(String email);

    User findByName(String username);
}
