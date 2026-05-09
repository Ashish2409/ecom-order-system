package com.ashish.ecom.user_service.dto;

import com.ashish.ecom.user_service.model.User;
import lombok.Builder;
import lombok.Getter;

// dto/AuthResponse.java
@Getter
@Builder
public class AuthResponse {
    private final Long userId;
    private final String name;
    private final String email;
    private final String role;
    private final String token;
    private final String tokenType;
    private final long expiresIn;

    public static AuthResponse from(User user, String token) {
        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400)  // seconds
                .build();
    }
}
