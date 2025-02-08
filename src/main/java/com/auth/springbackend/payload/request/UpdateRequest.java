package com.auth.springbackend.payload.request;

import com.auth.springbackend.security.password.ValidPassword;
import lombok.Data;

@Data
public class UpdateRequest {
    @ValidPassword
    private String password;
    private String fullname;
    //private String description;
    //private String image;
}