package com.auth.springbackend.payload.response;

import com.auth.springbackend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateResponse {
    private boolean success;
    private String message;
    private User user;
}