package com.example.vpbankdemopersonal.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendLoginEvent(String username, String status) {
        var payload = "{\"username\":\"" + username + "\",\"status\":\"" + status + "\"}";
        kafkaTemplate.send("user-login-event", payload);
    }
}
