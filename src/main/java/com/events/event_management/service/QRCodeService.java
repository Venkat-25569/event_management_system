  package com.events.event_management.service;

import com.events.event_management.model.Event;
import com.events.event_management.repository.EventRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRCodeService {

    @Autowired
    private EventRepository eventRepository;

    /**
     * Generate QR Code for Event Details (No Photo Required)
     * QR Code will open event-details.html page showing event information
     */
    public byte[] generateQRCode(Long eventId) throws WriterException, IOException {
        // Verify event exists
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        
        System.out.println("Generating QR code for event: " + event.getTitle());
        
        // Create event details URL (shows info only, no attendance marking)
        String eventDetailsUrl = String.format(
            "http://localhost:8080/pages/event-details.html?eventId=%d",
            eventId
        );
        
        System.out.println("QR Code URL: " + eventDetailsUrl);
        
        // Generate QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            eventDetailsUrl,
            BarcodeFormat.QR_CODE,
            400,  // Width
            400   // Height
        );
        
        // Convert to PNG image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        System.out.println("QR code generated successfully for event ID: " + eventId);
        
        return outputStream.toByteArray();
    }
    
    /**
     * Generate QR Code with custom URL
     */
    public byte[] generateQRCodeWithUrl(String url) throws WriterException, IOException {
        System.out.println("Generating QR code for URL: " + url);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            url,
            BarcodeFormat.QR_CODE,
            400,
            400
        );
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        return outputStream.toByteArray();
    }
}