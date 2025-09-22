package org.example.aidetectorbe.controllers;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.dto.ErrorResponse;
import org.example.aidetectorbe.logger.Log;
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
@AllArgsConstructor
public class AIModelController {

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    // TODO: Inject AI model service here when created

    @PostMapping(value = "/useModel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> useModel(@RequestParam("image") MultipartFile image, HttpServletRequest request) {
        String authenticatedUser = (String) request.getAttribute("login");
        Log.info("Received request to analyze image with AI model from user: " + authenticatedUser);
        
        long startTime = System.currentTimeMillis();
        
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
            
            // TODO: Call AI model service to process the image
            // AIModelResponse result = aiModelService.processImage(image);
            
            // Temporary response with processing time
            long processingTime = System.currentTimeMillis() - startTime;
            AIModelResponse response = new AIModelResponse(
                "Image processed successfully", 
                0.95, 
                "TempModel-v1.0", 
                processingTime
            );
            
            Log.info("Image analysis completed successfully in " + processingTime + "ms");
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
                
        } catch (Exception e) {
            Log.error("Error processing image: " + e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse("Internal Server Error", "Failed to process image", 500);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
}
