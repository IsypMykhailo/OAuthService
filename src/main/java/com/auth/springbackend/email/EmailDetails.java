package com.auth.springbackend.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor

// Class
public class EmailDetails {

    // Class data members
    private String from;
    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;
    private String template;
    Map<String, Object> properties;
}