package com.auth.springbackend.payload.request.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FullNameRequest {
    @NotBlank
    private String fullname;
}
