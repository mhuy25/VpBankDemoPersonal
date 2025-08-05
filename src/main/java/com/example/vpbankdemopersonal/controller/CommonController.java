package com.example.vpbankdemopersonal.controller;

import com.example.vpbankdemopersonal.dto.ProductRequest;
import com.example.vpbankdemopersonal.entity.Products;
import com.example.vpbankdemopersonal.entity.RefreshToken;
import com.example.vpbankdemopersonal.repository.RefreshTokenRepository;
import com.example.vpbankdemopersonal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common")
public class CommonController {
    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepo;

    @PreAuthorize("hasAuthority('VIEW')")
    @PostMapping("/products")
    public ResponseEntity<?> getAllProducts(@RequestBody ProductRequest req) {
        try {
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
            List<Products> productsList = authService.getAllProducts();
            token.setLastActivityTime(LocalDateTime.now());
            refreshTokenRepo.save(token);
            return ResponseEntity.ok(productsList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fetch data fail: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('VIEW')")
    @PostMapping("/getproduct")
    public ResponseEntity<?> getProducts(@RequestBody ProductRequest req) {
        try {
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
            Products product = authService.getProductByName(req.getProductName());
            token.setLastActivityTime(LocalDateTime.now());
            refreshTokenRepo.save(token);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fetch data fail: " + e.getMessage());
        }
    }
}
