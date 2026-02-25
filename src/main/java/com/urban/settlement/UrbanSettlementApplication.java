package com.urban.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Urban Settlement Mapping and Service Management System
 * HIT 400 - Final Year Project
 * 
 * Main application entry point
 */
@SpringBootApplication
//@EnableMongoRepositories(basePackages = "com.urban.settlement.repository")
//@EnableMongoAuditing
public class UrbanSettlementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbanSettlementApplication.class, args);
    }
}
