package org.example.repository.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.user.LoginRequest;
import org.example.dto.request.user.RegisterRequest;
import org.example.dto.response.user.auth.AuthResponse;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.example.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest request) {
        var user = userMapper.fromRegisterRequest(request);
        userRepository.save(user);

        var claims = new HashMap<String, Object>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole() == null ? null : user.getRole().name());
        var token = jwtService.generateToken(claims, user);
        return new AuthResponse(token);
    }


    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        var claims = new HashMap<String, Object>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole() == null ? null : user.getRole().name());
        String token = jwtService.generateToken(claims, user);
        return new AuthResponse(token);
    }
}

