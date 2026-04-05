  package com.events.event_management.service;

import com.events.event_management.model.Event;
import com.events.event_management.model.EventRegistration;
import com.events.event_management.repository.EventRegistrationRepository;
import com.events.event_management.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RegistrationService {
    
    @Autowired
    private EventRegistrationRepository registrationRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    // Register for event
    public Map<String, Object> registerForEvent(Long eventId, String studentEmail, 
                                                 String studentName, String rollNumber, 
                                                 String department) {
        Map<String, Object> response = new HashMap<>();
        
        // Check if event exists
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Event not found");
            return response;
        }
        
        Event event = eventOpt.get();
        
        // Check if event is approved
        if (!"APPROVED".equals(event.getStatus())) {
            response.put("success", false);
            response.put("message", "Event is not approved yet");
            return response;
        }
        
        // Check if already registered
        if (registrationRepository.existsByEventIdAndStudentEmail(eventId, studentEmail)) {
            response.put("success", false);
            response.put("message", "You are already registered for this event");
            return response;
        }
        
        // Create registration
        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setStudentEmail(studentEmail);
        registration.setStudentName(studentName);
        registration.setRollNumber(rollNumber);
        registration.setDepartment(department);
        
        // Payment logic - For demo: Events with ID > 5 cost ₹100
        BigDecimal eventFee = BigDecimal.ZERO;
        if (eventId > 1) {
            eventFee = new BigDecimal("100.00");
        }
        registration.setAmount(eventFee);
        registration.setPaymentStatus(eventFee.compareTo(BigDecimal.ZERO) > 0 ? "PENDING" : "FREE");
        registration.setStatus("REGISTERED");
        
        EventRegistration saved = registrationRepository.save(registration);
        
        response.put("success", true);
        response.put("message", "Successfully registered for " + event.getTitle());
        response.put("registrationId", saved.getId());
        response.put("eventTitle", event.getTitle());
        response.put("amount", saved.getAmount());
        response.put("paymentStatus", saved.getPaymentStatus());
        
        return response;
    }
    
    // Get student's registrations
    public List<Map<String, Object>> getStudentRegistrations(String studentEmail) {
        List<EventRegistration> registrations = registrationRepository.findByStudentEmail(studentEmail);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (EventRegistration reg : registrations) {
            Optional<Event> eventOpt = eventRepository.findById(reg.getEventId());
            if (eventOpt.isPresent()) {
                Event event = eventOpt.get();
                Map<String, Object> item = new HashMap<>();
                item.put("registrationId", reg.getId());
                item.put("eventId", event.getId());
                item.put("eventTitle", event.getTitle());
                item.put("eventDate", event.getEventDate());
                item.put("venue", event.getVenue());
                item.put("registrationDate", reg.getRegistrationDate());
                item.put("paymentStatus", reg.getPaymentStatus());
                item.put("amount", reg.getAmount());
                item.put("status", reg.getStatus());
                result.add(item);
            }
        }
        
        return result;
    }
    
    // Cancel registration
    public Map<String, Object> cancelRegistration(Long registrationId, String studentEmail) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<EventRegistration> regOpt = registrationRepository.findById(registrationId);
        if (!regOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Registration not found");
            return response;
        }
        
        EventRegistration registration = regOpt.get();
        
        // Verify ownership
        if (!registration.getStudentEmail().equals(studentEmail)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }
        
        registrationRepository.delete(registration);
        
        response.put("success", true);
        response.put("message", "Registration cancelled successfully");
        
        return response;
    }
    
    // Get event registrations count
    public long getEventRegistrationsCount(Long eventId) {
        List<EventRegistration> registrations = registrationRepository.findByEventId(eventId);
        return registrations.size();
    }
}