package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.entities.ModelResult;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.example.aidetectorbe.repository.ModelResultRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.utils.logger.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ModelAnalysisFlowService {

    private final AIModelService aiModelService;
    private final UserRepository userRepository;
    private final ModelResultRepository modelResultRepository;
    private final PhotoStorageService photoStorageService;
    private final DataSize maxFileSize;

    public ModelAnalysisFlowService(
            AIModelService aiModelService,
            UserRepository userRepository,
            ModelResultRepository modelResultRepository,
            PhotoStorageService photoStorageService,
            @Value("${spring.servlet.multipart.max-file-size:10MB}") DataSize maxFileSize) {
        this.aiModelService = aiModelService;
        this.userRepository = userRepository;
        this.modelResultRepository = modelResultRepository;
        this.photoStorageService = photoStorageService;
        this.maxFileSize = maxFileSize;
    }

    public AIModelResponse analyzeAndStore(MultipartFile image, String authenticatedUser) throws AIServiceException {
        validateAuthenticatedUser(authenticatedUser);
        validateImage(image);

        AIModelResponse response = aiModelService.processImage(image);

        User user = userRepository.findByLogin(authenticatedUser)
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));

        UUID photoId = photoStorageService.storeAndGetPhotoId(image);

        ModelResult result = new ModelResult();
        result.setPhotoId(photoId);
        result.setUserId(user.getId());
        result.setModel(response.getModelUsed());
        result.setChance(toChance(response.getCertainty()));
        modelResultRepository.save(result);

        Log.info("Stored model analysis result for user " + authenticatedUser + " and photoId " + photoId);
        return response;
    }

    private void validateAuthenticatedUser(String authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.isBlank()) {
            throw new SecurityException("Missing authenticated user");
        }
    }

    private void validateImage(MultipartFile image) {
        try {
            if (image == null || image.isEmpty() || image.getSize() == 0) {
                throw new IllegalArgumentException("Empty file provided");
            }

            long maxFileSizeBytes = maxFileSize.toBytes();
            if (image.getSize() > maxFileSizeBytes) {
                throw new IllegalArgumentException("File size too large. Maximum allowed size is " + maxFileSize);
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }

            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getBytes()));
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Provided file was not an image");
            }

            Log.info("Processing image: " + image.getOriginalFilename() + " (" + image.getSize() + " bytes)");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to validate image");
        }
    }

    private BigDecimal toChance(Double certainty) {
        double value = certainty == null ? 0.0 : certainty * 100.0;
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}