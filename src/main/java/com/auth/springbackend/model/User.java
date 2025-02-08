package com.auth.springbackend.model;
import com.auth.springbackend.security.password.ValidPassword;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Data
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    /*@OneToOne(mappedBy = "user")
    private RefreshToken refreshToken;*/

    @Email
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullname;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean active;


   /* @Column(nullable = false)
    private Boolean emailVerified = false;*/


    @JsonIgnore
    @ValidPassword
    private String password;

   /* @Column(name = "last_login")
    private Date lastLogin;*/

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @CreationTimestamp
    private Date created_at; // = new Date();

    @LastModifiedDate
    private Date updated_at; // = new Date();

}
