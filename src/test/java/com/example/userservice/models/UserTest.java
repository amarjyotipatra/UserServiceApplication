package com.example.userservice.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    @Test
    public void testUser() {
        User user = new User();
        user.setName("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setVerified(true);

        assertEquals("testuser", user.getName());
        assertEquals("testuser@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(true, user.isVerified());
    }
}

