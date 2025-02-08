package com.auth.springbackend.payload.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Data
public class LoginRequest {
    @Email
    private String email;

    @NotBlank
    private String password;

    private String login;
}