package com.example.vpbankdemopersonal.controller;

import com.example.vpbankdemopersonal.dto.LoginRequest;
import com.example.vpbankdemopersonal.dto.ProductRequest;
import com.example.vpbankdemopersonal.dto.RegisterRequest;
import com.example.vpbankdemopersonal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common")
public class CommonController {
    private final AuthService authService;

    @PostMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        try {
            return ResponseEntity.ok(authService.getAllProducts());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fetch data fail: " + e.getMessage());
        }
    }

    @PostMapping("/getproduct")
    public ResponseEntity<?> getProducts(@RequestBody ProductRequest req) {
        try {
            return ResponseEntity.ok(authService.getProductByName(req.getProductName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fetch data fail: " + e.getMessage());
        }
    }
}
