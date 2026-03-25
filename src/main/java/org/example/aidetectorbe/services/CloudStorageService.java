package org.example.aidetectorbe.services;

import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {

    /**
     * Uploads an image to cloud storage.
     *
     * @param file           the image file to upload
     * @param uniqueFileName the unique filename to use for storage
     * @return the URL of the uploaded image
     * @throws Exception if the upload fails
     */
    String uploadImage(MultipartFile file, String uniqueFileName) throws Exception;
}
