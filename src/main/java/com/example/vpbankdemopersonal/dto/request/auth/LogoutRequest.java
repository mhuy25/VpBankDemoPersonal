package com.example.vpbankdemopersonal.dto.request.auth;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
