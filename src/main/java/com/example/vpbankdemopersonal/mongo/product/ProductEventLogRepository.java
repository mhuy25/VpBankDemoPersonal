package com.example.vpbankdemopersonal.mongo.product;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductEventLogRepository extends MongoRepository<ProductEventLog, String> {}
