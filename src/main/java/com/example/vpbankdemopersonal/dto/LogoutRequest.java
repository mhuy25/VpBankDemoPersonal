package com.example.vpbankdemopersonal.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
