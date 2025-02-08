package com.auth.springbackend.repository;

import com.auth.springbackend.model.token.PasswordResetToken;
import com.auth.springbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findPasswordResetTokenByToken(String confirmationToken);
    void deletePasswordResetTokenByToken(String token);
    void deleteAllByUser(User user);
}
