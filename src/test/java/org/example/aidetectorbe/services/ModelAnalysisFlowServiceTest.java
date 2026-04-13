package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.entities.ModelResult;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.ModelResultRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModelAnalysisFlowServiceTest {

    private AIModelService aiModelService;
    private UserRepository userRepository;
    private ModelResultRepository modelResultRepository;
    private PhotoStorageService photoStorageService;
    private ModelAnalysisFlowService flowService;

    @BeforeEach
    void setUp() {
        aiModelService = mock(AIModelService.class);
        userRepository = mock(UserRepository.class);
        modelResultRepository = mock(ModelResultRepository.class);
        photoStorageService = mock(PhotoStorageService.class);

        flowService = new ModelAnalysisFlowService(
                aiModelService,
                userRepository,
                modelResultRepository,
                photoStorageService,
                DataSize.ofMegabytes(5));
    }

    @Test
    void analyzeAndStore_ShouldSaveResultAndReturnResponse() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.png", "image/png", validPngBytes());
        AIModelResponse aiResponse = new AIModelResponse(0.95, "TestModel", 100L);
        UUID photoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        when(aiModelService.processImage(image)).thenReturn(aiResponse);
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(photoStorageService.storeAndGetPhotoId(image)).thenReturn(photoId);

        AIModelResponse result = flowService.analyzeAndStore(image, "testUser");

        verify(modelResultRepository).save(any(ModelResult.class));
        assertEquals(aiResponse, result);
    }

    @Test
    void analyzeAndStore_ShouldReturnBadRequestForNonImage() {
        MockMultipartFile image = new MockMultipartFile("image", "test.txt", "text/plain", "hello".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flowService.analyzeAndStore(image, "testUser"));

        assertEquals("File must be an image", exception.getMessage());
    }

    @Test
    void analyzeAndStore_ShouldMapCertaintyToPercentageChance() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.png", "image/png", validPngBytes());
        AIModelResponse aiResponse = new AIModelResponse(0.91, "TestModel", 100L);
        UUID photoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        when(aiModelService.processImage(image)).thenReturn(aiResponse);
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(photoStorageService.storeAndGetPhotoId(image)).thenReturn(photoId);

        flowService.analyzeAndStore(image, "testUser");

        verify(modelResultRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.getChance().compareTo(new BigDecimal("91.00")) == 0));
    }

    private byte[] validPngBytes() {
        return Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+tmw0AAAAASUVORK5CYII=");
    }
}



