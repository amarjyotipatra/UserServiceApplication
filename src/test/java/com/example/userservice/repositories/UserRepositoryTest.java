package com.example.userservice.repositories;

import com.example.userservice.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void testSaveAndFindByName() {
        User user = new User();
        user.setName("user-repo-test");
        user.setEmail("user-repo-test@example.com");
        user.setPassword("password");
        user.setVerified(false); // Add missing isVerified field

        entityManager.persist(user);
        entityManager.flush();

        User foundUser = userRepository.findByName("user-repo-test");

        assertNotNull(foundUser);
        assertEquals("user-repo-test", foundUser.getName());
    }
}
