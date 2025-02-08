package com.auth.springbackend.model.token;

import com.auth.springbackend.model.User;
import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Data
public class PasswordResetToken {

    private static final int EXPIRATION = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;
}
