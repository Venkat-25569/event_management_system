 package com.events.event_management.controller;

import com.events.event_management.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {
    
    @Autowired
    private RegistrationService registrationService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerForEvent(@RequestBody Map<String, Object> request) {
        Long eventId = Long.valueOf(request.get("eventId").toString());
        String studentEmail = request.get("studentEmail").toString();
        String studentName = request.get("studentName").toString();
        String rollNumber = request.getOrDefault("rollNumber", "").toString();
        String department = request.getOrDefault("department", "").toString();
        
        Map<String, Object> response = registrationService.registerForEvent(
            eventId, studentEmail, studentName, rollNumber, department
        );
        
        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/student/{email}")
    public ResponseEntity<?> getStudentRegistrations(@PathVariable String email) {
        List<Map<String, Object>> registrations = registrationService.getStudentRegistrations(email);
        return ResponseEntity.ok(registrations);
    }
    
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<?> cancelRegistration(
            @PathVariable Long registrationId,
            @RequestParam String studentEmail) {
        
        Map<String, Object> response = registrationService.cancelRegistration(registrationId, studentEmail);
        
        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<?> getEventRegistrationCount(@PathVariable Long eventId) {
        long count = registrationService.getEventRegistrationsCount(eventId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}