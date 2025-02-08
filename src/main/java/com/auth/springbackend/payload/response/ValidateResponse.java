package com.auth.springbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateResponse {
    private Object data;
    private boolean success;
    private String message;
}