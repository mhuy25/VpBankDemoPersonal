package com.example.vpbankdemopersonal.kafka.producer;

import com.example.vpbankdemopersonal.kafka.dto.KafkaMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void produceEvent(String topic, KafkaMessageDto message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(message);
            kafkaTemplate.send(topic, json);
        } catch (JsonProcessingException e) {
            log.info("Kafka send errors: {}", e.getMessage());
        }
    }
}
