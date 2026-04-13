package org.example.aidetectorbe.controllers;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.dto.ErrorResponse;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.example.aidetectorbe.services.ModelAnalysisFlowService;
import org.example.aidetectorbe.utils.logger.Log;
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
public class AIModelController {

    private final ModelAnalysisFlowService modelAnalysisFlowService;

    public AIModelController(ModelAnalysisFlowService modelAnalysisFlowService) {
        this.modelAnalysisFlowService = modelAnalysisFlowService;
    }

    @PostMapping(value = {"/useModel", "/model/analyze"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> useModel(@RequestParam("image") MultipartFile image, HttpServletRequest request) {
        String authenticatedUser = (String) request.getAttribute("login");
        Log.info("Received request to analyze image with AI model from user: " + authenticatedUser);
        
        try {
            AIModelResponse response = modelAnalysisFlowService.analyzeAndStore(image, authenticatedUser);

            Log.info("Image analysis completed successfully in " + response.getProcessingTimeMs() + "ms");
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
        } catch (IllegalArgumentException e) {
            Log.error("Invalid request for image analysis: " + e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse("Bad Request", e.getMessage(), 400);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
        } catch (SecurityException e) {
            Log.error("Unauthorized image analysis request: " + e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse("Unauthorized", e.getMessage(), 401);
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);

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
