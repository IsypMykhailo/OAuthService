package com.auth.springbackend.repository;

import com.auth.springbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);
    Optional<User> findByFullnameContaining(String fullname);
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByLogin(String email);

}
