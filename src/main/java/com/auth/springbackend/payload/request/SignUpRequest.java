package com.auth.springbackend.payload.request;

import com.auth.springbackend.security.password.ValidPassword;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class SignUpRequest {
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
