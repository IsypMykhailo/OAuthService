package com.auth.springbackend.payload.request.user;
import lombok.Data;

import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
@Data
public class IdRequest {
    @NotBlank
    private String id;
}
