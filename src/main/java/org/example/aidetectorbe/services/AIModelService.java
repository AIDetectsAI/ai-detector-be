package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.springframework.web.multipart.MultipartFile;

public interface AIModelService {
    AIModelResponse processImage(MultipartFile image) throws AIServiceException;

    AIModelResponse getPastQueryByImageId(String imageId, String username) throws AIServiceException;

    boolean isServiceHealthy();
}