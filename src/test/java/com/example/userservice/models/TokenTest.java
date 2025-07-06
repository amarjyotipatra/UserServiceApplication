package com.example.userservice.models;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenTest {

    @Test
    public void testToken() {
        Token token = new Token();
        User user = new User();
        user.setName("testuser");
        Date date = new Date();

        token.setToken("test_token");
        token.setUser(user);
        token.setExpired(false);
        token.setExpiredAt(date);

        assertEquals("test_token", token.getToken());
        assertEquals("testuser", token.getUser().getName());
        assertEquals(false, token.isExpired());
        assertEquals(date, token.getExpiredAt());
    }
}

