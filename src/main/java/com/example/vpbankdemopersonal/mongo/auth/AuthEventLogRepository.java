package com.example.vpbankdemopersonal.mongo.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuthEventLogRepository extends MongoRepository<AuthEventLog, String> {}
