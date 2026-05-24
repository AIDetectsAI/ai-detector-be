package org.example.aidetectorbe.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class PhotoStorageService {

    public UUID storeAndGetPhotoId(MultipartFile image) {
        try {
            // TODO: replace this with real cloud upload and return cloud object key-based identifier.
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(image.getBytes());
            ByteBuffer buffer = ByteBuffer.wrap(hash);
            return new UUID(buffer.getLong(), buffer.getLong());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate photo identifier", e);
        }
    }
}