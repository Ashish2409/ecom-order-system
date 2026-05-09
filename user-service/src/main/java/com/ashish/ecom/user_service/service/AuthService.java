package com.ashish.ecom.user_service.service;

import com.ashish.ecom.user_service.dto.AuthResponse;
import com.ashish.ecom.user_service.dto.LoginRequest;
import com.ashish.ecom.user_service.dto.RegisterRequest;

public interface AuthService{
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
