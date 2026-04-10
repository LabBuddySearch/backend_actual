package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.user.LoginRequest;
import org.example.dto.request.user.RegisterRequest;
import org.example.dto.response.user.auth.AuthResponse;
import org.example.repository.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and JWT issuance")
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user and get JWT")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}