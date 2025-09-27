package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.example.aidetectorbe.logger.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AIModelServiceImpl implements AIModelService {
    
    @Value("${ai.service.url}")
    private String aiServiceUrl;
    
    @Value("${ai.service.endpoint:/verify/image}")
    private String aiServiceEndpoint;
    
    @Value("${ai.service.timeout:30000}")
    private int timeoutMs;
    
    @Value("${ai.service.model-name:AIDetector}")
    private String modelName;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AIModelServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public AIModelResponse processImage(MultipartFile image) throws AIServiceException {
        long startTime = System.currentTimeMillis();
        
        Log.info("Sending image to AI service: " + aiServiceUrl + aiServiceEndpoint);
        
        try {
            // Prepare the request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // Create multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Convert MultipartFile to ByteArrayResource for RestTemplate
            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };
            
            body.add("image", imageResource);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Make the request
            String fullUrl = aiServiceUrl + aiServiceEndpoint;
            ResponseEntity<String> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            // Parse response
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseSuccessfulResponse(response.getBody(), startTime);
            } else {
                throw new AIServiceException("AI service returned error status: " + response.getStatusCode(), 
                    response.getStatusCode().value());
            }
            
        } catch (HttpClientErrorException e) {
            Log.error("Client error from AI service: " + e.getMessage());
            throw new AIServiceException("Invalid request to AI service: " + e.getMessage(), e, e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            Log.error("Server error from AI service: " + e.getMessage());
            throw new AIServiceException("AI service is experiencing issues: " + e.getMessage(), e, e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            Log.error("Network error connecting to AI service: " + e.getMessage());
            throw new AIServiceException("Unable to connect to AI service. Please try again later.", e, 503);
        } catch (AIServiceException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            Log.error("Unexpected error communicating with AI service: " + e.getMessage());
            throw new AIServiceException("Failed to process image with AI service: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isServiceHealthy() {
        try {
            String healthUrl = aiServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            Log.warn("AI service health check failed: " + e.getMessage());
            return false;
        }
    }
    
    private AIModelResponse parseSuccessfulResponse(String responseBody, long startTime) throws AIServiceException {
        try {
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            // Extract response fields - adjust these based on your AI service's response format
            String result = jsonResponse.has("prediction") ? 
                jsonResponse.get("prediction").asText() : 
                "Image processed successfully";
                
            Double confidence = jsonResponse.has("confidence") ? 
                jsonResponse.get("confidence").asDouble() : 
                null;
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            Log.info("AI service response parsed successfully in " + processingTime + "ms");
            
            return new AIModelResponse(
                result,
                confidence,
                modelName,
                processingTime
            );
            
        } catch (Exception e) {
            Log.error("Failed to parse AI service response: " + e.getMessage());
            throw new AIServiceException("Invalid response format from AI service", e, 502);
        }
    }
}