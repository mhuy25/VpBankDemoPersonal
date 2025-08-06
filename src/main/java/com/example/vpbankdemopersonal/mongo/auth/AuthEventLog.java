package com.example.vpbankdemopersonal.mongo.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("auth_event_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthEventLog {
    @Id
    private String id;
    private String username;
    private String status;
    private String event;
    private LocalDateTime timestamp;
}
