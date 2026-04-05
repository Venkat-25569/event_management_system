package com.events.event_management.controller;

import com.events.event_management.model.Event;
import com.events.event_management.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*")
public class CalendarController {
    
    @Autowired
    private EventService eventService;
    
    @GetMapping("/events")
    public ResponseEntity<?> getCalendarEvents() {
        List<Event> events = eventService.getAllEvents();
        
        // Convert events to FullCalendar format
        List<Map<String, Object>> calendarEvents = events.stream().map(event -> {
            Map<String, Object> calEvent = new HashMap<>();
            calEvent.put("id", event.getId());
            calEvent.put("title", event.getTitle());
            calEvent.put("start", event.getEventDate().toString());
            calEvent.put("description", event.getDescription());
            calEvent.put("venue", event.getVenue());
            calEvent.put("club", event.getClubName());
            calEvent.put("organizer", event.getOrganizerName());
            calEvent.put("participants", event.getParticipantCount());
            
            // Color code by status
            String status = event.getStatus() != null ? event.getStatus() : "PENDING";
            switch (status) {
                case "APPROVED":
                    calEvent.put("backgroundColor", "#28a745");
                    calEvent.put("borderColor", "#28a745");
                    break;
                case "REJECTED":
                    calEvent.put("backgroundColor", "#dc3545");
                    calEvent.put("borderColor", "#dc3545");
                    break;
                default:
                    calEvent.put("backgroundColor", "#ffc107");
                    calEvent.put("borderColor", "#ffc107");
                    break;
            }
            
            return calEvent;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(calendarEvents);
    }
}