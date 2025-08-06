package com.targetcv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main Spring Boot Application class for TargetCV
 * Resume Targeting Tool - Java Spring Boot Implementation
 */
@SpringBootApplication
@EnableConfigurationProperties
public class TargetCvApplication {

    public static void main(String[] args) {
        SpringApplication.run(TargetCvApplication.class, args);
    }
}
