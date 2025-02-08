package com.auth.springbackend.payload.request;

import com.auth.springbackend.security.password.ValidPassword;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordRequest {
    @NotBlank
    @ValidPassword
    private String password;
    @NotBlank
    private String passwordResetToken;
}
