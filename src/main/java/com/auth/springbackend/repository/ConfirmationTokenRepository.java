package com.auth.springbackend.repository;

import com.auth.springbackend.model.token.ConfirmationToken;
import com.auth.springbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("confirmationTokenRepository")
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    ConfirmationToken findByConfirmationToken(String confirmationToken);
    void deleteConfirmationTokenByConfirmationToken(String token);
    void deleteAllByUser(User user);
}
