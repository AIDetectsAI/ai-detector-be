package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.entities.ModelResult;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.example.aidetectorbe.repository.ModelResultRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.utils.Constants;
import org.example.aidetectorbe.utils.logger.Log;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.UUID;

@Service
public class AIModelServiceImpl implements AIModelService {
    
    @Value("${ai.service.url:http://localhost:8000}")
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

    @Autowired
    private ModelResultRepository modelResultRepository;

    @Autowired
    private UserRepository userRepository;

    public AIModelServiceImpl() {
        this(new RestTemplate(), new ObjectMapper());
    }

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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

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

            HttpHeaders filePartHeaders = new HttpHeaders();
            filePartHeaders.setContentDispositionFormData(aiServiceFileField, image.getOriginalFilename());
            filePartHeaders.setContentType(MediaType.parseMediaType(image.getContentType() != null ? image.getContentType() : "application/octet-stream"));

            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(imageResource, filePartHeaders);

            body.add(aiServiceFileField, filePart);
            body.add("type", "image");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
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
            throw e;
        } catch (Exception e) {
            Log.error("Unexpected error communicating with AI service: " + e.getMessage());
            throw new AIServiceException("Failed to process image with AI service: " + e.getMessage(), e);
        }
    }
    
    @Override
    public AIModelResponse getPastQueryByImageId(String imageId, String username) throws AIServiceException {
        try {
            if (username == null) {
                throw new AIServiceException("Unauthorized", 401);
            }

            User user = userRepository.findByLoginAndProvider(username, Constants.AI_DETECTOR_API_PROVIDER)
                .orElseThrow(() -> new AIServiceException("Forbidden", 403));

            UUID photoId;
            try {
                photoId = UUID.fromString(imageId);
            } catch (IllegalArgumentException e) {
                throw new AIServiceException("Invalid imageId format", 400);
            }

            ModelResult record = modelResultRepository.findByPhotoIdAndUserId(photoId, user.getId())
                    .orElseThrow(() -> new AIServiceException("Query not found", 404));

            Double certainty = record.getChance() != null ? record.getChance().doubleValue() : null;
            return new AIModelResponse(certainty, record.getModel(), null, record.getPhotoId().toString());
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AIServiceException("Failed to retrieve past query", e);
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
            
            Double certainty = jsonResponse.has("certainty") ?
                jsonResponse.get("certainty").asDouble() :
                null;

            long processingTime = System.currentTimeMillis() - startTime;
            
            Log.info("AI service response parsed successfully in " + processingTime + "ms");
            
            return new AIModelResponse(
                certainty,
                modelName,
                processingTime,
                null
            );
            
        } catch (Exception e) {
            Log.error("Failed to parse AI service response: " + e.getMessage());
            throw new AIServiceException("Invalid response format from AI service", e, 502);
        }
    }
}