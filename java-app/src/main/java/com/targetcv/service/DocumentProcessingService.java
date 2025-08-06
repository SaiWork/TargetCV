package com.targetcv.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for processing document uploads and text extraction
 */
@Service
public class DocumentProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);
    
    private final Tika tika;
    
    @Value("${targetcv.upload.directory:uploads/}")
    private String uploadDirectory;
    
    @Value("${targetcv.upload.allowed-extensions:pdf,docx,txt,doc}")
    private String allowedExtensions;
    
    public DocumentProcessingService() {
        this.tika = new Tika();
    }
    
    /**
     * Save uploaded file to disk and return file information
     */
    public FileInfo saveUploadedFile(MultipartFile file) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("File saved: {} -> {}", originalFilename, uniqueFilename);
        
        return new FileInfo(
            uniqueFilename,
            originalFilename,
            filePath.toString(),
            file.getSize(),
            file.getContentType()
        );
    }
    
    /**
     * Extract text content from uploaded file
     */
    public String extractTextFromFile(String filePath) throws IOException, TikaException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        
        try {
            String extractedText = tika.parseToString(file);
            logger.info("Text extracted from file: {} ({} characters)", 
                       file.getName(), extractedText.length());
            
            return cleanExtractedText(extractedText);
        } catch (Exception e) {
            logger.error("Error extracting text from file: {}", filePath, e);
            throw new TikaException("Failed to extract text from file", e);
        }
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IOException("Invalid filename");
        }
        
        String fileExtension = getFileExtension(originalFilename);
        List<String> allowedExtensionsList = Arrays.asList(allowedExtensions.split(","));
        
        if (!allowedExtensionsList.contains(fileExtension.toLowerCase())) {
            throw new IOException("File type not allowed. Allowed types: " + allowedExtensions);
        }
        
        // Check file size (16MB limit)
        long maxFileSize = 16 * 1024 * 1024; // 16MB
        if (file.getSize() > maxFileSize) {
            throw new IOException("File size exceeds maximum limit of 16MB");
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        
        return "";
    }
    
    /**
     * Clean extracted text by removing extra whitespace and formatting
     */
    private String cleanExtractedText(String text) {
        if (text == null) {
            return "";
        }
        
        // Remove excessive whitespace and normalize line breaks
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("\\n\\s*\\n", "\n\n");
        text = text.trim();
        
        return text;
    }
    
    /**
     * Delete file from disk
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                logger.info("File deleted: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Error deleting file: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Get file size in bytes
     */
    public long getFileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }
    
    /**
     * Inner class to hold file information
     */
    public static class FileInfo {
        private final String filename;
        private final String originalFilename;
        private final String filePath;
        private final Long fileSize;
        private final String contentType;
        
        public FileInfo(String filename, String originalFilename, String filePath, 
                       Long fileSize, String contentType) {
            this.filename = filename;
            this.originalFilename = originalFilename;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }
        
        // Getters
        public String getFilename() { return filename; }
        public String getOriginalFilename() { return originalFilename; }
        public String getFilePath() { return filePath; }
        public Long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
    }
}
