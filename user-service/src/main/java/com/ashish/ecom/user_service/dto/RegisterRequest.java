package com.ashish.ecom.user_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

// RegisterRequest.java
@Data
public class RegisterRequest {
    @NotBlank  private String name;
    @Email @NotBlank private String email;
    @Size(min = 6) private String password;
}
