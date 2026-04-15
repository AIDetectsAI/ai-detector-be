package org.example.aidetectorbe.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.aidetectorbe.utils.logger.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "cloud.provider", havingValue = "cloudinary")
public class CloudinaryStorageServiceImpl implements CloudStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImpl(
            @Value("${cloud.cloudinary.cloud-name}") String cloudName,
            @Value("${cloud.cloudinary.api-key}") String apiKey,
            @Value("${cloud.cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    @Override
    public String uploadImage(MultipartFile file, String uniqueFileName) throws Exception {
        Log.info("Cloudinary: Uploading file: " + uniqueFileName);

        // Remove extension from uniqueFileName to use as public_id
        String publicId = uniqueFileName.contains(".")
                ? uniqueFileName.substring(0, uniqueFileName.lastIndexOf("."))
                : uniqueFileName;

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "ai-detector",
                "resource_type", "image"));

        String imageUrl = (String) uploadResult.get("secure_url");
        Log.info("Cloudinary: File uploaded successfully. URL: " + imageUrl);

        return imageUrl;
    }
}
