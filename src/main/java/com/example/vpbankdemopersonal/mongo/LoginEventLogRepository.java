package com.example.vpbankdemopersonal.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginEventLogRepository extends MongoRepository<LoginEventLog, String> {}
