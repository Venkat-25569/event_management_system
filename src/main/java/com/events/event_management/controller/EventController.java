   package com.events.event_management.controller;

import com.events.event_management.model.Event;
import com.events.event_management.repository.EventRepository;
import com.events.event_management.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all events
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        try {
            List<Event> events = eventRepository.findAll();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            System.err.println("Error fetching events: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        try {
            Optional<Event> event = eventRepository.findById(id);
            return event.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error fetching event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create new event
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVENT_COORDINATOR')")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            // Set initial status as PENDING
            event.setStatus("PENDING");
            event.setHodApproved(false);
            event.setCoordinatorApproved(false);
            event.setCreatedAt(LocalDateTime.now());
            
            Event savedEvent = eventRepository.save(event);
            
            System.out.println("Event created: " + savedEvent.getTitle() + " (ID: " + savedEvent.getId() + ")");
            
            return ResponseEntity.ok(savedEvent);
            
        } catch (Exception e) {
            System.err.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create event: " + e.getMessage()));
        }
    }

    /**
     * Update event
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVENT_COORDINATOR')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        try {
            Optional<Event> eventOpt = eventRepository.findById(id);
            
            if (eventOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Event event = eventOpt.get();
            
            // Update fields
            if (eventDetails.getTitle() != null) event.setTitle(eventDetails.getTitle());
            if (eventDetails.getDescription() != null) event.setDescription(eventDetails.getDescription());
            if (eventDetails.getEventDate() != null) event.setEventDate(eventDetails.getEventDate());
            if (eventDetails.getVenue() != null) event.setVenue(eventDetails.getVenue());
            if (eventDetails.getClubName() != null) event.setClubName(eventDetails.getClubName());
            if (eventDetails.getOrganizerName() != null) event.setOrganizerName(eventDetails.getOrganizerName());
            if (eventDetails.getParticipantCount() != null) event.setParticipantCount(eventDetails.getParticipantCount());
            
            Event updatedEvent = eventRepository.save(event);
            
            System.out.println("Event updated: " + updatedEvent.getTitle());
            
            return ResponseEntity.ok(updatedEvent);
            
        } catch (Exception e) {
            System.err.println("Error updating event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update event"));
        }
    }

    /**
     * Delete event
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            if (!eventRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            eventRepository.deleteById(id);
            
            System.out.println("Event deleted: ID " + id);
            
            return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
            
        } catch (Exception e) {
            System.err.println("Error deleting event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete event"));
        }
    }

    /**
     * DUAL APPROVAL SYSTEM - FIXED VERSION
     * Approve event - requires both HOD and EVENT_COORDINATOR approval
     * Status remains PENDING until both approve, then changes to APPROVED
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('HOD', 'EVENT_COORDINATOR', 'ADMIN')")
    public ResponseEntity<?> approveEvent(@PathVariable Long id, 
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract user info from token
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            System.out.println("=== APPROVAL REQUEST ===");
            System.out.println("User: " + email + " (Role: " + role + ")");
            System.out.println("Event ID: " + id);
            
            // Get the event
            Optional<Event> eventOpt = eventRepository.findById(id);
            if (eventOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Event not found"));
            }
            
            Event event = eventOpt.get();
            
            System.out.println("Event: " + event.getTitle());
            System.out.println("Current Status: " + event.getStatus());
            System.out.println("HOD Approved: " + event.getHodApproved());
            System.out.println("Coordinator Approved: " + event.getCoordinatorApproved());
            
            // Check if already fully approved
            if ("APPROVED".equals(event.getStatus())) {
                return ResponseEntity.ok(Map.of(
                    "message", "Event is already fully approved",
                    "status", "APPROVED",
                    "hodApproved", true,
                    "coordinatorApproved", true
                ));
            }
            
            // ADMIN can approve directly (bypass dual approval)
            if ("ADMIN".equals(role)) {
                event.setHodApproved(true);
                event.setHodApprovedBy(email);
                event.setHodApprovedAt(LocalDateTime.now());
                event.setCoordinatorApproved(true);
                event.setCoordinatorApprovedBy(email);
                event.setCoordinatorApprovedAt(LocalDateTime.now());
                event.setStatus("APPROVED");
                
                eventRepository.save(event);
                
                System.out.println("✅ ADMIN approved event directly: " + event.getTitle());
                
                return ResponseEntity.ok(Map.of(
                    "message", "✅ Event fully approved by ADMIN",
                    "status", "APPROVED",
                    "hodApproved", true,
                    "coordinatorApproved", true,
                    "fullyApproved", true
                ));
            }
            
            // HOD approval
            if ("HOD".equals(role)) {
                if (Boolean.TRUE.equals(event.getHodApproved())) {
                    return ResponseEntity.ok(Map.of(
                        "message", "You have already approved this event",
                        "status", event.getStatus(),
                        "hodApproved", true,
                        "coordinatorApproved", event.getCoordinatorApproved()
                    ));
                }
                
                event.setHodApproved(true);
                event.setHodApprovedBy(email);
                event.setHodApprovedAt(LocalDateTime.now());
                
                // Check if coordinator has also approved
                if (Boolean.TRUE.equals(event.getCoordinatorApproved())) {
                    event.setStatus("APPROVED");
                    System.out.println("✅ Event FULLY APPROVED (Both approved): " + event.getTitle());
                } else {
                    // Keep status as PENDING (not PENDING_COORDINATOR_APPROVAL)
                    event.setStatus("PENDING");
                    System.out.println("⏳ HOD approved. Status remains PENDING. Waiting for EVENT_COORDINATOR: " + event.getTitle());
                }
            }
            
            // EVENT_COORDINATOR approval
            else if ("EVENT_COORDINATOR".equals(role)) {
                if (Boolean.TRUE.equals(event.getCoordinatorApproved())) {
                    return ResponseEntity.ok(Map.of(
                        "message", "You have already approved this event",
                        "status", event.getStatus(),
                        "hodApproved", event.getHodApproved(),
                        "coordinatorApproved", true
                    ));
                }
                
                event.setCoordinatorApproved(true);
                event.setCoordinatorApprovedBy(email);
                event.setCoordinatorApprovedAt(LocalDateTime.now());
                
                // Check if HOD has also approved
                if (Boolean.TRUE.equals(event.getHodApproved())) {
                    event.setStatus("APPROVED");
                    System.out.println("✅ Event FULLY APPROVED (Both approved): " + event.getTitle());
                } else {
                    // Keep status as PENDING (not PENDING_HOD_APPROVAL)
                    event.setStatus("PENDING");
                    System.out.println("⏳ EVENT_COORDINATOR approved. Status remains PENDING. Waiting for HOD: " + event.getTitle());
                }
            }
            
            // Save event
            Event savedEvent = eventRepository.save(event);
            
            System.out.println("Final Status: " + savedEvent.getStatus());
            System.out.println("======================");
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("message", getApprovalMessage(savedEvent));
            response.put("status", savedEvent.getStatus());
            response.put("hodApproved", savedEvent.getHodApproved());
            response.put("coordinatorApproved", savedEvent.getCoordinatorApproved());
            response.put("fullyApproved", "APPROVED".equals(savedEvent.getStatus()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error approving event: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to approve event: " + e.getMessage()));
        }
    }

    private String getApprovalMessage(Event event) {
        if ("APPROVED".equals(event.getStatus())) {
            return "✅ Event fully approved by both HOD and EVENT_COORDINATOR!";
        } else if (Boolean.TRUE.equals(event.getHodApproved()) && !Boolean.TRUE.equals(event.getCoordinatorApproved())) {
            return "✅ Your approval recorded. Waiting for EVENT_COORDINATOR approval.";
        } else if (Boolean.TRUE.equals(event.getCoordinatorApproved()) && !Boolean.TRUE.equals(event.getHodApproved())) {
            return "✅ Your approval recorded. Waiting for HOD approval.";
        }
        return "✅ Approval recorded";
    }

    /**
     * Reject event - Any one can reject (HOD, COORDINATOR, or ADMIN)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('HOD', 'EVENT_COORDINATOR', 'ADMIN')")
    public ResponseEntity<?> rejectEvent(@PathVariable Long id,
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            System.out.println("Rejection by: " + email + " (Role: " + role + ") for event ID: " + id);
            
            Optional<Event> eventOpt = eventRepository.findById(id);
            if (eventOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Event not found"));
            }
            
            Event event = eventOpt.get();
            event.setStatus("REJECTED");
            
            eventRepository.save(event);
            
            System.out.println("❌ Event rejected: " + event.getTitle());
            
            return ResponseEntity.ok(Map.of(
                "message", "Event rejected",
                "status", "REJECTED"
            ));
            
        } catch (Exception e) {
            System.err.println("Error rejecting event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to reject event"));
        }
    }

    /**
     * Get events by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Event>> getEventsByStatus(@PathVariable String status) {
        try {
            List<Event> events = eventRepository.findByStatus(status);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            System.err.println("Error fetching events by status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}