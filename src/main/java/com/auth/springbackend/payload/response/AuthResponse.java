package com.auth.springbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class    AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType; //= "Bearer"

}
