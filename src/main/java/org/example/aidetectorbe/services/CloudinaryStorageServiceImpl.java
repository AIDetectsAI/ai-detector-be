package org.example.aidetectorbe.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.aidetectorbe.utils.logger.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

                byte[] fileBytes = file.getBytes();

                try {
                        return uploadBytesToCloudinary(fileBytes, publicId, uniqueFileName, file);
                } catch (Exception firstError) {
                        boolean isPng = "image/png".equalsIgnoreCase(file.getContentType())
                                        || (uniqueFileName != null && uniqueFileName.toLowerCase().endsWith(".png"));

                        if (isPng) {
                                Log.warn("Cloudinary: Upload failed for PNG, retrying after normalizing to JPEG",
                                                firstError);
                                try {
                                        byte[] jpegBytes = normalizeToJpeg(fileBytes);
                                        return uploadBytesToCloudinary(jpegBytes, publicId, uniqueFileName, file);
                                } catch (Exception retryError) {
                                        retryError.addSuppressed(firstError);
                                        Log.error(
                                                        "Cloudinary: Upload retry after JPEG normalization failed for uniqueFileName="
                                                                        + uniqueFileName,
                                                        retryError);
                                        throw retryError;
                                }
                        }

                        Log.error(
                                        "Cloudinary: Upload failed for uniqueFileName=" + uniqueFileName
                                                        + " (contentType="
                                                        + file.getContentType() + ", size=" + file.getSize()
                                                        + " bytes)",
                                        firstError);
                        throw firstError;
                }
        }

        private String uploadBytesToCloudinary(byte[] bytes, String publicId, String uniqueFileName, MultipartFile file)
                        throws Exception {
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                                "public_id", publicId,
                                "folder", "ai-detector",
                                "resource_type", "image"));

                String imageUrl = (String) uploadResult.get("secure_url");
                if (imageUrl == null || imageUrl.isBlank()) {
                        throw new IllegalStateException("Cloudinary did not return secure_url");
                }

                Log.info("Cloudinary: File uploaded successfully. URL: " + imageUrl + " (uniqueFileName="
                                + uniqueFileName
                                + ", contentType=" + file.getContentType() + ", size=" + file.getSize() + " bytes)");

                return imageUrl;
        }

        private byte[] normalizeToJpeg(byte[] imageBytes) throws IOException {
                BufferedImage input = ImageIO.read(new ByteArrayInputStream(imageBytes));
                if (input == null) {
                        throw new IOException("Unable to decode image for JPEG normalization");
                }

                BufferedImage rgb = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = rgb.createGraphics();
                try {
                        graphics.setColor(Color.WHITE);
                        graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
                        graphics.drawImage(input, 0, 0, null);
                } finally {
                        graphics.dispose();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                boolean written = ImageIO.write(rgb, "jpg", outputStream);
                if (!written) {
                        throw new IOException("No JPEG writer available for ImageIO");
                }
                return outputStream.toByteArray();
        }
}
