package com.example.vpbankdemopersonal.kafka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class KafkaLoginMessage extends KafkaMessageDto {
    String username;
    String status;
}
