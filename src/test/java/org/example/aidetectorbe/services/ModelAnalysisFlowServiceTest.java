package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.entities.ModelResult;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.ModelResultRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.services.CloudStorageService;
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
    private CloudStorageService cloudStorageService;
    private ModelAnalysisFlowService flowService;

    @BeforeEach
    void setUp() {
        aiModelService = mock(AIModelService.class);
        userRepository = mock(UserRepository.class);
        modelResultRepository = mock(ModelResultRepository.class);
        cloudStorageService = mock(CloudStorageService.class);

        flowService = new ModelAnalysisFlowService(
                aiModelService,
                userRepository,
                modelResultRepository,
                cloudStorageService,
                DataSize.ofMegabytes(5));
    }

    @Test
    void analyzeAndStore_ShouldSaveResultAndReturnResponse() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.png", "image/png", validPngBytes());
        AIModelResponse aiResponse = new AIModelResponse(0.95, "TestModel", 100L);
        String photoUrl = "http://example.com/photo.jpg";
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        when(aiModelService.processImage(image)).thenReturn(aiResponse);
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(cloudStorageService.uploadImage(any(), any())).thenReturn(photoUrl);

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
// Note: The test for mapping certainty to percentage chance is outdated because we no longer store photoId.
// We'll update it to use the new photoUrl field, but for now we can comment it out or adjust.
        // Since we removed photoId, we need to adjust this test. However, the chance mapping is independent of photo storage.
        // We can still test the chance by mocking the cloudStorageService and then checking the saved ModelResult's chance.
        // Let's update the test to use cloudStorageService and then verify the chance.
        // We'll keep the test but change the mock and the argument capture.

        flowService.analyzeAndStore(image, "testUser");

        verify(modelResultRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.getChance().compareTo(new BigDecimal("91.00")) == 0));
    }

    private byte[] validPngBytes() {
        return Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+tmw0AAAAASUVORK5CYII=");
    }
}



