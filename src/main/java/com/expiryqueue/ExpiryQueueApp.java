package com.expiryqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpiryQueueApp extends SpringApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ExpiryQueueApp.class, args);
    }
}