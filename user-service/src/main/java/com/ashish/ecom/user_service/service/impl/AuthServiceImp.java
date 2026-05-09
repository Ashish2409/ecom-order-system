package com.ashish.ecom.user_service.service.impl;

import com.ashish.ecom.user_service.dto.AuthResponse;
import com.ashish.ecom.user_service.dto.LoginRequest;
import com.ashish.ecom.user_service.dto.RegisterRequest;
import com.ashish.ecom.user_service.exception.DuplicateResourceException;
import com.ashish.ecom.user_service.exception.InvalidCredentialsException;
import com.ashish.ecom.user_service.model.User;
import com.ashish.ecom.user_service.repository.UserRepository;
import com.ashish.ecom.user_service.security.JwtUtil;
import com.ashish.ecom.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static String sanitizeForLog(String value) {
        if (value == null) return "";
        return value.replace('\r', '_').replace('\n', '_');
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        String safeEmail = sanitizeForLog(request.getEmail());
        log.info("Registering new user with email: {}", maskEmail(safeEmail));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully with id: {}", saved.getId());

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name(), saved.getId());
        return AuthResponse.from(saved, token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        log.info("User logged in: {}", user.getId());
        return AuthResponse.from(user, token);
    }
}
