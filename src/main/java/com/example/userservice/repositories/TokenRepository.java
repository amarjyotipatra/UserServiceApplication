package com.example.userservice.repositories;

import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    // Find token regardless of status (for logout)
    Optional<Token> findByTokenAndIsDeletedFalse(String token);

    // Find active tokens (not deleted and not expired)
    Optional<Token> findByTokenAndIsDeletedFalseAndIsExpiredFalse(String token);

    // Find all active tokens for a user
    List<Token> findByUserAndIsDeletedFalseAndIsExpiredFalse(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Token t SET t.isDeleted = true WHERE t.token = :token AND t.isDeleted = false")
    int markTokenAsDeleted(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("UPDATE Token t SET t.isDeleted = true WHERE t.user = :user AND t.isDeleted = false")
    int markAllUserTokensAsDeleted(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("UPDATE Token t SET t.isExpired = true WHERE t.expiredAt < CURRENT_TIMESTAMP AND t.isExpired = false")
    int markExpiredTokens();
}
