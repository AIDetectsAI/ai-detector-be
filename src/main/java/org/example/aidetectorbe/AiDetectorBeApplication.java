package org.example.aidetectorbe;

import org.example.aidetectorbe.logger.Log;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static org.example.aidetectorbe.basicClientToDelete.saveRandomUser;

@SpringBootApplication
public class AiDetectorBeApplication {

    public static void main(String[] args) {
       saveRandomUser();
       Log.info("Application started successfully.");
    }
}
