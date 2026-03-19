package org.example.aidetectorbe;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.aidetectorbe.logger.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiDetectorBeApplication {

    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            Log.error("Error loading .env file: " + e.getMessage());
        }

        SpringApplication.run(AiDetectorBeApplication.class, args);
        Log.info("Application started successfully.");
    }
}
