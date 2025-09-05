package org.example.aidetectorbe;

import org.example.aidetectorbe.logger.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiDetectorBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDetectorBeApplication.class, args);
        Log.info("Application started successfully.");
    }
}
