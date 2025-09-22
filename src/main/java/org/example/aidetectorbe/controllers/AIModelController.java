package org.example.aidetectorbe.controllers;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.logger.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AIModelController {

    // TODO: Inject AI model service here when created

    @PostMapping(value = "/useModel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> useModel(@RequestParam("image") MultipartFile image, HttpServletRequest request) {
        String authenticatedUser = (String) request.getAttribute("login");
        Log.info("Received request to analyze image with AI model from user: " + authenticatedUser);
        
        try {
            if (image.isEmpty()) {
                Log.error("No image file provided");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Bad Request\", \"message\": \"No image file provided\", \"status\": 400}");
            }
            
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Log.error("Invalid file type: " + contentType);
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Bad Request\", \"message\": \"File must be an image\", \"status\": 400}");
            }
            
            Log.info("Processing image: " + image.getOriginalFilename() + " (" + image.getSize() + " bytes)");
            
            // TODO: Call AI model service to process the image
            // String result = aiModelService.processImage(image);
            
            // Temporary response
            String result = "{\"result\": \"Image processed successfully\", \"confidence\": 0.95}";
            
            Log.info("Image analysis completed successfully");
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
                
        } catch (Exception e) {
            Log.error("Error processing image: " + e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal Server Error\", \"message\": \"Failed to process image\", \"status\": 500}");
        }
    }
}
