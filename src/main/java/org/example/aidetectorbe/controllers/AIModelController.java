package org.example.aidetectorbe.controllers;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.dto.ErrorResponse;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.example.aidetectorbe.logger.Log;
import org.example.aidetectorbe.services.AIModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class AIModelController {

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    @Autowired
    private AIModelService aiModelService;

    @PostMapping(value = "/useModel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> useModel(@RequestParam("image") MultipartFile image, HttpServletRequest request) {
        String authenticatedUser = (String) request.getAttribute("login");
        Log.info("Received request to analyze image with AI model from user: " + authenticatedUser);
        
        try {
            // Validate image file
            if (image.isEmpty()) {
                Log.error("No image file provided");
                ErrorResponse errorResponse = new ErrorResponse("Bad Request", "No image file provided", 400);
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
            }
            
            // Validate file size (using configured limit)
            long maxFileSizeBytes = maxFileSize.toBytes();
            if (image.getSize() > maxFileSizeBytes) {
                Log.error("File size too large: " + image.getSize() + " bytes (max: " + maxFileSizeBytes + " bytes)");
                ErrorResponse errorResponse = new ErrorResponse("Bad Request", 
                    "File size too large. Maximum allowed size is " + maxFileSize.toString(), 400);
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
            }
            
            // Validate file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Log.error("Invalid file type: " + contentType);
                ErrorResponse errorResponse = new ErrorResponse("Bad Request", "File must be an image", 400);
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
            }
            
            Log.info("Processing image: " + image.getOriginalFilename() + " (" + image.getSize() + " bytes)");
            
            // Call AI model service to process the image
            AIModelResponse response = aiModelService.processImage(image);
            
            Log.info("Image analysis completed successfully in " + response.getProcessingTimeMs() + "ms");
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
                
        } catch (AIServiceException e) {
            Log.error("AI service error: " + e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse("AI Service Error", e.getMessage(), e.getStatusCode());
            return ResponseEntity
                .status(HttpStatus.valueOf(e.getStatusCode()))
                .body(errorResponse);
        } catch (Exception e) {
            Log.error("Unexpected error processing image: " + e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse("Internal Server Error", "Failed to process image", 500);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
}
