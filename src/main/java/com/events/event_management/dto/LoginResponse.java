 package com.events.event_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String message;
}