package com.auth.springbackend.payload.request.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ListUserRequest {
    List<String> userList;
}
