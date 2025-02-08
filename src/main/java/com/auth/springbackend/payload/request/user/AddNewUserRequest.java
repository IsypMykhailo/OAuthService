package com.auth.springbackend.payload.request.user;

import com.auth.springbackend.security.password.ValidPassword;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class AddNewUserRequest {
    @NotBlank
    private String login;

    @NotBlank
    private String fullname;

    @NotBlank
    @Email
    private String email;

    @ValidPassword
    private String password;
}
