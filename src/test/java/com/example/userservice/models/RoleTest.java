package com.example.userservice.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoleTest {

    @Test
    public void testRole() {
        Role role = new Role();
        role.setRoleName("ADMIN");

        assertEquals("ADMIN", role.getRoleName());
    }
}

