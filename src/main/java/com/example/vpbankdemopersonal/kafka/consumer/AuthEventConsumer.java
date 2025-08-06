package com.example.vpbankdemopersonal.kafka.consumer;

import com.example.vpbankdemopersonal.kafka.topic.KafkaTopic;
import com.example.vpbankdemopersonal.mongo.auth.AuthEventLog;
import com.example.vpbankdemopersonal.mongo.auth.AuthEventLogRepository;
import com.example.vpbankdemopersonal.mongo.product.ProductEventLog;
import com.example.vpbankdemopersonal.mongo.product.ProductEventLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventConsumer {
    private final AuthEventLogRepository AuthRepo;
    private final ProductEventLogRepository ProductRepo;
    private final ObjectMapper mapper;

    @KafkaListener(topics = {
            KafkaTopic.LOG_IN_USER,
            KafkaTopic.REGISTER_USER,
    }, groupId = "${kafka.group.auth}")
    public void consumeAuthEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic , String message) {
        try {
            initAuthLogData(topic, message);
        } catch (Exception e) {
            errorMessage(topic, e.getMessage());
        }
    }

    @KafkaListener(topics = {
            KafkaTopic.SEARCH_PRODUCT_BY_NAME,
            KafkaTopic.SEARCH_PRODUCT_ALL,
    }, groupId = "${kafka.group.product}")
    public void consumeProductEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic , String message) {
        try {
            initProductLogData(topic, message);
        } catch (Exception e) {
            errorMessage(topic, e.getMessage());
        }
    }

    private void initAuthLogData(String event, String message) throws JsonProcessingException {
        JsonNode json = mapper.readTree(message);
        AuthEventLog log = new AuthEventLog();
        log.setUsername(json.get("username").asText());
        log.setStatus(json.get("status").asText());
        log.setEvent(event);
        log.setTimestamp(LocalDateTime.now());
        AuthRepo.save(log);
    }

    private void initProductLogData(String event, String message) throws JsonProcessingException {
        JsonNode json = mapper.readTree(message);
        ProductEventLog log = new ProductEventLog();
        log.setUsername(json.get("username").asText());
        log.setStatus(json.get("status").asText());
        log.setEvent(event);
        log.setTimestamp(LocalDateTime.now());
        ProductRepo.save(log);
    }

    private void errorMessage(String event, String message) {
        log.info("Kafka#{} listen errors: {}", event, message);
    }
}
