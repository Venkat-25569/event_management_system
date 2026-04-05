package com.events.event_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    public String storeFile(MultipartFile file, String category) {
        try {
            // Create directory if not exists
            Path uploadPath = Paths.get(uploadDir + "/" + category);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path
            return category + "/" + uniqueFilename;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
    
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir + "/" + filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
    
    public byte[] loadFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir + "/" + filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file", e);
        }
    }
}