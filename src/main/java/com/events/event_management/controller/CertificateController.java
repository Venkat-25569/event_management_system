package com.events.event_management.controller;

import com.events.event_management.model.Event;
import com.events.event_management.service.CertificateService;
import com.events.event_management.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "*")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private EventService eventService;

    @GetMapping("/generate/{eventId}")
    public ResponseEntity<byte[]> generateCertificate(
            @PathVariable Long eventId,
            @RequestParam String participantName) {
        try {
            Optional<Event> eventOpt = eventService.getEventById(eventId);

            if (!eventOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Event event = eventOpt.get();

            // Only generate for approved events
            if (!"APPROVED".equals(event.getStatus())) {
                return ResponseEntity.badRequest().build();
            }

            byte[] certificate = certificateService
                .generateCertificate(event, participantName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set("Content-Disposition",
                "attachment; filename=certificate-" +
                eventId + "-" +
                participantName.replace(" ", "_") + ".pdf");

            return new ResponseEntity<>(certificate, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("Error generating certificate: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}