 package com.events.event_management.controller;

import com.events.event_management.model.Event;
import com.events.event_management.service.EventService;
import com.events.event_management.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private EventService eventService;
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "eventId", required = false) Long eventId) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validate file size (10MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File size exceeds 10MB limit");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Store file
            String filePath = fileStorageService.storeFile(file, category);
            
            // Update event if eventId provided
            if (eventId != null) {
                Event event = eventService.getEventById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
                
                switch (category) {
                    case "reports":
                        event.setReportPath(filePath);
                        break;
                    case "posters":
                        event.setPosterPath(filePath);
                        break;
                    case "photos":
                        event.setPhotoPath(filePath);
                        break;
                }
                
                eventService.updateEvent(eventId, event);
            }
            
            // Return success response
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("filePath", filePath);
            response.put("fileUrl", "/uploads/" + filePath);
            response.put("fileName", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("filePath") String filePath) {
        try {
            fileStorageService.deleteFile(filePath);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}