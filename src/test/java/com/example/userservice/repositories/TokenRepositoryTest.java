package com.example.userservice.repositories;

import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    public void testFindByTokenAndIsDeletedFalse() {
        User user = new User();
        user.setName("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setVerified(false); // Add missing isVerified field
        entityManager.persist(user);

        Token token = new Token();
        token.setToken("test_token");
        token.setUser(user);
        token.setExpired(false);
        token.setExpiredAt(new Date(System.currentTimeMillis() + 3600000));
        entityManager.persist(token);
        entityManager.flush();

        Optional<Token> foundToken = tokenRepository.findByTokenAndIsDeletedFalse("test_token");

        assertTrue(foundToken.isPresent());
        assertEquals("test_token", foundToken.get().getToken());
    }
}
