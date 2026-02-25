package com.urban.settlement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.urban.settlement.config.FileStorageConfig;
import com.urban.settlement.model.enums.IssueCategory;
import com.urban.settlement.model.enums.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * AI-powered image classification service
 * Classifies infrastructure issues and predicts severity using CNN model
 * 
 * Objective 1: Automatic classification & severity prediction
 * Target: 92%+ accuracy, 2-3 seconds processing time
 */
@Service
public class ImageClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(ImageClassificationService.class);
    private static final String PYTHON_SCRIPT_PATH = "ml_scripts/classify_image.py";

    @Autowired
    private PythonExecutorService pythonExecutor;

    @Autowired
    private FileStorageConfig fileStorageConfig;

    /**
     * Classify uploaded image and predict severity
     * 
     * @param image Uploaded image file
     * @return ClassificationResult with category, severity, and confidence
     * @throws IOException if classification fails
     */
    public ClassificationResult classifyImage(MultipartFile image) throws IOException {

        logger.info("Starting image classification for file: {}", image.getOriginalFilename());

        // 1. Save image to file system
        String imagePath = saveImage(image);

        try {
            // 2. Execute Python classification script
            long startTime = System.currentTimeMillis();

            JsonNode result = pythonExecutor.executePythonScript(
                    PYTHON_SCRIPT_PATH,
                    5, // 5 second timeout
                    imagePath);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Classification completed in {} ms", duration);

            // 3. Parse results
            String category = result.get("category").asText();
            String severity = result.get("severity").asText();
            double confidence = result.get("confidence").asDouble();

            return new ClassificationResult(
                    IssueCategory.valueOf(category),
                    Severity.valueOf(severity),
                    confidence,
                    imagePath);

        } catch (IOException e) {
            // Clean up image if classification fails
            deleteImage(imagePath);
            throw new IOException("Image classification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Save uploaded image to file system
     * 
     * @param file Uploaded file
     * @return Relative path to saved image
     * @throws IOException if save fails
     */
    private String saveImage(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IOException("Cannot save empty file");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String filename = UUID.randomUUID().toString() + extension;
        String relativePath = "uploads/issues/" + filename;

        Path uploadPath = Paths.get(fileStorageConfig.getUploadDir());
        Path filePath = uploadPath.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Image saved to: {}", filePath.toAbsolutePath());

        return relativePath;
    }

    /**
     * Delete image from file system
     */
    private void deleteImage(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path);
            logger.info("Deleted image: {}", imagePath);
        } catch (IOException e) {
            logger.error("Failed to delete image: {}", imagePath, e);
        }
    }

    /**
     * Classification result DTO
     */
    public static class ClassificationResult {
        private final IssueCategory category;
        private final Severity severity;
        private final double confidence;
        private final String imagePath;

        public ClassificationResult(IssueCategory category, Severity severity,
                double confidence, String imagePath) {
            this.category = category;
            this.severity = severity;
            this.confidence = confidence;
            this.imagePath = imagePath;
        }

        public IssueCategory getCategory() {
            return category;
        }

        public Severity getSeverity() {
            return severity;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getImagePath() {
            return imagePath;
        }
    }
}
