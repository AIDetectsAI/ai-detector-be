package org.example.aidetectorbe.services;

import org.example.aidetectorbe.logger.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "cloud.provider", havingValue = "mock", matchIfMissing = true)
public class MockCloudStorageServiceImpl implements CloudStorageService {

    private static final String MOCK_BASE_URL = "https://mock-cloud-storage.example.com/images/";
    private static final long SIMULATED_DELAY_MS = 500;

    @Override
    public String uploadImage(MultipartFile file, String uniqueFileName) throws Exception {
        Log.info("MockCloudStorage: Simulating upload for file: " + uniqueFileName);

        // Simulate network delay
        Thread.sleep(SIMULATED_DELAY_MS);

        String mockUrl = MOCK_BASE_URL + uniqueFileName;
        Log.info("MockCloudStorage: Upload simulated successfully. URL: " + mockUrl);

        return mockUrl;
    }
}
