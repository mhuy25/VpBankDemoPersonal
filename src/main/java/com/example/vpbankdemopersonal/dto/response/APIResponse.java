package com.example.vpbankdemopersonal.dto.response;

import com.example.vpbankdemopersonal.dto.response.APIResponseDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PROTECTED)
public class APIResponse {
    String path;
    String status;
    Long timestamp = System.currentTimeMillis();
    String message;
    APIResponseDto data;
}
