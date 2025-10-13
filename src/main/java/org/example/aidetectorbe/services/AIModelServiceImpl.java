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

    @Value("${ai.service.file-field:file}")
    private String aiServiceFileField;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Default constructor used by Spring in production
    public AIModelServiceImpl() {
        this(new RestTemplate(), new ObjectMapper());
    }

    // Constructor for tests to inject mockable RestTemplate/ObjectMapper
    public AIModelServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public AIModelResponse processImage(MultipartFile image) throws AIServiceException {
        long startTime = System.currentTimeMillis();
        
        Log.info("=== AI Service Call Debug ===");
        Log.info("Target URL: " + aiServiceUrl + aiServiceEndpoint);
        Log.info("Image filename: " + image.getOriginalFilename());
        Log.info("Image size: " + image.getSize() + " bytes");
        Log.info("Image content type: " + image.getContentType());
        
        try {
            // Prepare the request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // Create multipart body and attach the file under configured field name
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Convert MultipartFile to ByteArrayResource for RestTemplate
            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
                @Override
                public long contentLength() {
                    return image.getSize();
                }
            };

            // Use an HttpEntity wrapper for the file part so we can set part headers explicitly
            HttpHeaders filePartHeaders = new HttpHeaders();
            filePartHeaders.setContentDispositionFormData(aiServiceFileField, image.getOriginalFilename());
            filePartHeaders.setContentType(MediaType.parseMediaType(image.getContentType() != null ? image.getContentType() : "application/octet-stream"));

            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(imageResource, filePartHeaders);

            body.add(aiServiceFileField, filePart);
            body.add("type", "image");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Make the request
            String fullUrl = aiServiceUrl + aiServiceEndpoint;
            Log.info("Making POST request to: " + fullUrl);
            
            ResponseEntity<String> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            Log.info("AI service response status: " + response.getStatusCode());
            Log.info("AI service response body: " + response.getBody());
            
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

            Double certainty = jsonResponse.has("certainty") ?
                jsonResponse.get("certainty").asDouble() :
                null;
            String result = certainty != null ? (certainty >= 0.5 ? "AI-Generated" : "Human-Made") : "Image processed successfully";

            long processingTime = System.currentTimeMillis() - startTime;
            
            Log.info("AI service response parsed successfully in " + processingTime + "ms");
            
            return new AIModelResponse(
                result,
                certainty,
                modelName,
                processingTime
            );
            
        } catch (Exception e) {
            Log.error("Failed to parse AI service response: " + e.getMessage());
            throw new AIServiceException("Invalid response format from AI service", e, 502);
        }
    }
}