package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.springframework.web.multipart.MultipartFile;

public interface AIModelService {
    
    /**
     * Process an image using the external AI model service
     * 
     * @param image The image file to process
     * @return AIModelResponse containing the analysis results
     * @throws AIServiceException if processing fails
     */
    AIModelResponse processImage(MultipartFile image) throws AIServiceException;
    
    /**
     * Check if the AI model service is available
     * 
     * @return true if service is healthy, false otherwise
     */
    boolean isServiceHealthy();
}