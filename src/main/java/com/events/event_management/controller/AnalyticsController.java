  package com.events.event_management.controller;

import com.events.event_management.model.Event;
import com.events.event_management.service.EventService;
import com.events.event_management.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private ClubService clubService;
    
    @GetMapping("/events-by-month")
    public ResponseEntity<Map<String, Object>> getEventsByMonth() {
        List<Event> events = eventService.getAllEvents();
        
        Map<String, Long> monthCounts = events.stream()
            .filter(e -> e.getEventDate() != null)
            .collect(Collectors.groupingBy(
                e -> e.getEventDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                Collectors.counting()
            ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("labels", new ArrayList<>(monthCounts.keySet()));
        response.put("data", new ArrayList<>(monthCounts.values()));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/events-by-club")
    public ResponseEntity<Map<String, Object>> getEventsByClub() {
        List<Event> events = eventService.getAllEvents();
        
        Map<String, Long> clubCounts = events.stream()
            .filter(e -> e.getClubName() != null && !e.getClubName().isEmpty())
            .collect(Collectors.groupingBy(
                Event::getClubName,
                Collectors.counting()
            ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("labels", new ArrayList<>(clubCounts.keySet()));
        response.put("data", new ArrayList<>(clubCounts.values()));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/events-by-status")
    public ResponseEntity<Map<String, Object>> getEventsByStatus() {
        List<Event> events = eventService.getAllEvents();
        
        Map<String, Long> statusCounts = events.stream()
            .collect(Collectors.groupingBy(
                Event::getStatus,
                Collectors.counting()
            ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("labels", new ArrayList<>(statusCounts.keySet()));
        response.put("data", new ArrayList<>(statusCounts.values()));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Event> events = eventService.getAllEvents();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalEvents", events.size());
        summary.put("pendingEvents", events.stream().filter(e -> "PENDING".equals(e.getStatus())).count());
        summary.put("approvedEvents", events.stream().filter(e -> "APPROVED".equals(e.getStatus())).count());
        summary.put("rejectedEvents", events.stream().filter(e -> "REJECTED".equals(e.getStatus())).count());
        summary.put("totalParticipants", events.stream()
            .filter(e -> e.getParticipantCount() != null)
            .mapToInt(Event::getParticipantCount)
            .sum());
        summary.put("totalClubs", clubService.getAllClubs().size());
        
        return ResponseEntity.ok(summary);
    }
}