    package com.events.event_management.service;

import com.events.event_management.model.Event;
import com.events.event_management.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private EmailService emailService;
    
    // Create event
    public Event createEvent(Event event) {
        Event savedEvent = eventRepository.save(event);
        
        // Send email notification (find organizer's email)
        // For demo, we'll use a placeholder email
        String organizerEmail = event.getOrganizerName() + "@college.edu";
        emailService.sendEventCreatedEmail(savedEvent, organizerEmail);
        
        return savedEvent;
    }
    
    // Get all events
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
    // Get event by ID
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }
    
    // Update event
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setEventDate(eventDetails.getEventDate());
        event.setVenue(eventDetails.getVenue());
        event.setParticipantCount(eventDetails.getParticipantCount());
        
        return eventRepository.save(event);
    }
    
    // Delete event
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
    
    // Get events by status
    public List<Event> getEventsByStatus(String status) {
        return eventRepository.findByStatus(status);
    }
    
    // Approve event
    public Event approveEvent(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        event.setStatus("APPROVED");
        Event approvedEvent = eventRepository.save(event);
        
        // Send approval email
        String organizerEmail = event.getOrganizerName() + "@college.edu";
        emailService.sendEventApprovedEmail(approvedEvent, organizerEmail);
        
        return approvedEvent;
    }
    
    // Reject event
    public Event rejectEvent(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        event.setStatus("REJECTED");
        Event rejectedEvent = eventRepository.save(event);
        
        // Send rejection email
        String organizerEmail = event.getOrganizerName() + "@college.edu";
        emailService.sendEventRejectedEmail(rejectedEvent, organizerEmail, "Event does not meet approval criteria");
        
        return rejectedEvent;
    }
}