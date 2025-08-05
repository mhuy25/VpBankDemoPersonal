package com.example.vpbankdemopersonal.controller;

import com.example.vpbankdemopersonal.dto.LoginRequest;
import com.example.vpbankdemopersonal.dto.LogoutRequest;
import com.example.vpbankdemopersonal.dto.RegisterRequest;
import com.example.vpbankdemopersonal.entity.RefreshToken;
import com.example.vpbankdemopersonal.repository.RefreshTokenRepository;
import com.example.vpbankdemopersonal.security.CustomUserDetailsService;
import com.example.vpbankdemopersonal.security.JwtUtils;
import com.example.vpbankdemopersonal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepo;
    private final JwtUtils jwtUtils;
    private CustomUserDetailsService customUserDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = authService.login(req);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            authService.register(req);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest req) {
        authService.revoke(req.getRefreshToken());
        return ResponseEntity.ok("Logout success");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody LogoutRequest req) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepo.findByRefreshToken(req.getRefreshToken());
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
        RefreshToken token = tokenOpt.get();

        if (token.getExpiredTime().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
        }

        int inactivityLimitMinutes = 30;
        if (token.getLastActivityTime() != null && token.getLastActivityTime().plusMinutes(inactivityLimitMinutes).isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired due to inactivity");
        }

        String newAccessToken = jwtUtils.generateToken(customUserDetailsService.getUserDetailById(token.getUserId()));
        token.setLastActivityTime(LocalDateTime.now());
        refreshTokenRepo.save(token);

        return ResponseEntity.ok(Collections.singletonMap("token", newAccessToken));
    }
}
