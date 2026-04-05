 package com.events.event_management.controller;

import com.events.event_management.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
@CrossOrigin(origins = "*")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    /**
     * Generate QR Code for Event
     * GET /api/qrcode/generate/{eventId}
     */
     
    @GetMapping("/generate/{eventId}")
public ResponseEntity<byte[]> generateQRCode(@PathVariable Long eventId) {
    try {
        System.out.println("QR Code generation requested for event ID: " + eventId);
        
        byte[] qrCode = qrCodeService.generateQRCode(eventId);
        
        System.out.println("QR code generated. Size: " + qrCode.length + " bytes");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.set("Content-Disposition", "attachment; filename=event-" + eventId + "-qr.png");
        headers.setContentLength(qrCode.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(qrCode);
            
    } catch (Exception e) {
        System.err.println("Error generating QR code: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
    /**
     * Generate QR Code with custom URL
     * POST /api/qrcode/generate-url
     * Body: { "url": "https://example.com" }
     */
    @PostMapping("/generate-url")
    public ResponseEntity<byte[]> generateQRCodeWithUrl(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");
            
            if (url == null || url.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("QR Code generation requested for URL: " + url);
            
            byte[] qrCode = qrCodeService.generateQRCodeWithUrl(url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "qrcode.png");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(qrCode);
                
        } catch (Exception e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Test endpoint to verify QR code service is working
     * GET /api/qrcode/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "QR Code service is running");
        response.put("message", "Use /api/qrcode/generate/{eventId} to generate QR codes");
        return ResponseEntity.ok(response);
    }
}