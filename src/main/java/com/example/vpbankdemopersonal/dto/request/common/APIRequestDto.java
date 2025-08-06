package com.example.vpbankdemopersonal.dto.request.common;

import lombok.Data;

@Data
public abstract class APIRequestDto {
    protected String requestUserAgent;
    protected String requestIp;
    protected Double requestLatitude;
    protected Double requestLongitude;
}
