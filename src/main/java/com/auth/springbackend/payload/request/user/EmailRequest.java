package com.auth.springbackend.payload.request.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailRequest {
    @Email
    @NotBlank
    private String email;
}
