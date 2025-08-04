package com.example.vpbankdemopersonal.kafka.consumer;

import com.example.vpbankdemopersonal.mongo.LoginEventLog;
import com.example.vpbankdemopersonal.mongo.LoginEventLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuthEventConsumer {
    private final LoginEventLogRepository loginRepo;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "user-login-event", groupId = "auth-group")
    public void consumeLoginEvent(String message) throws JsonProcessingException {
        JsonNode json = mapper.readTree(message);
        LoginEventLog log = new LoginEventLog();
        log.setUsername(json.get("username").asText());
        log.setStatus(json.get("status").asText());
        log.setTimestamp(LocalDateTime.now());
        loginRepo.save(log);
    }
}
