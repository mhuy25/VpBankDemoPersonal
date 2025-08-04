package com.example.vpbankdemopersonal.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("login_event_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginEventLog {
    @Id
    private String id;
    private String username;
    private String status;
    private LocalDateTime timestamp;
}
