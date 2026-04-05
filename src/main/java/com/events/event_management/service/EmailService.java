 package com.events.event_management.service;

import com.events.event_management.model.Event;
import com.events.event_management.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@eventmanagement.com}")
    private String fromEmail;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    
    public void sendWelcomeEmail(User user) {
        logger.info("========================================");
        logger.info("📧 WELCOME EMAIL NOTIFICATION");
        logger.info("========================================");
        logger.info("To: {}", user.getEmail());
        logger.info("User: {}", user.getFullName());
        logger.info("Role: {}", user.getRole());
        
        if (mailSender == null) {
            logger.warn("⚠️ Email not configured - Mail will not be sent");
            logger.info("Would send welcome email to: {}", user.getEmail());
            logger.info("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Event Management System!");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Welcome to Event Management System!\n\n" +
                "Your account has been successfully created.\n" +
                "Username: %s\n" +
                "Role: %s\n" +
                "Department: %s\n\n" +
                "Best regards,\n" +
                "Event Management Team",
                user.getFullName(),
                user.getUsername(),
                user.getRole(),
                user.getDepartment()
            ));
            
            mailSender.send(message);
            logger.info("✅ Welcome email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
        logger.info("========================================");
    }
    
    public void sendEventCreatedEmail(Event event, String organizerEmail) {
        logger.info("========================================");
        logger.info("📧 EVENT CREATED EMAIL NOTIFICATION");
        logger.info("========================================");
        logger.info("Event: {}", event.getTitle());
        logger.info("To: {}", organizerEmail);
        logger.info("Organizer: {}", event.getOrganizerName());
        logger.info("Status: {}", event.getStatus());
        
        if (mailSender == null) {
            logger.warn("⚠️ Email not configured - Mail will not be sent");
            logger.info("Would send event created email to: {}", organizerEmail);
            logger.info("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(organizerEmail);
            message.setSubject("Event Created: " + event.getTitle());
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your event has been successfully created!\n\n" +
                "Event: %s\n" +
                "Date: %s\n" +
                "Venue: %s\n" +
                "Status: %s\n\n" +
                "Your event is pending approval.\n\n" +
                "Best regards,\n" +
                "Event Management Team",
                event.getOrganizerName(),
                event.getTitle(),
                event.getEventDate() != null ? event.getEventDate().format(DATE_FORMATTER) : "N/A",
                event.getVenue(),
                event.getStatus()
            ));
            
            mailSender.send(message);
            logger.info("✅ Event created email sent successfully to: {}", organizerEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send event created email to {}: {}", organizerEmail, e.getMessage());
        }
        logger.info("========================================");
    }
    
    public void sendEventApprovedEmail(Event event, String organizerEmail) {
        logger.info("========================================");
        logger.info("📧 EVENT APPROVED EMAIL NOTIFICATION");
        logger.info("========================================");
        logger.info("Event: {}", event.getTitle());
        logger.info("To: {}", organizerEmail);
        
        if (mailSender == null) {
            logger.warn("⚠️ Email not configured - Mail will not be sent");
            logger.info("Would send approval email to: {}", organizerEmail);
            logger.info("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(organizerEmail);
            message.setSubject("✅ Event Approved: " + event.getTitle());
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Great news! Your event has been APPROVED!\n\n" +
                "Event: %s\n" +
                "Date: %s\n" +
                "Venue: %s\n\n" +
                "You can now proceed with arrangements.\n\n" +
                "Best regards,\n" +
                "Event Management Team",
                event.getOrganizerName(),
                event.getTitle(),
                event.getEventDate() != null ? event.getEventDate().format(DATE_FORMATTER) : "N/A",
                event.getVenue()
            ));
            
            mailSender.send(message);
            logger.info("✅ Event approval email sent successfully to: {}", organizerEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send approval email to {}: {}", organizerEmail, e.getMessage());
        }
        logger.info("========================================");
    }
    
    public void sendEventRejectedEmail(Event event, String organizerEmail, String reason) {
        logger.info("========================================");
        logger.info("📧 EVENT REJECTED EMAIL NOTIFICATION");
        logger.info("========================================");
        logger.info("Event: {}", event.getTitle());
        logger.info("To: {}", organizerEmail);
        logger.info("Reason: {}", reason);
        
        if (mailSender == null) {
            logger.warn("⚠️ Email not configured - Mail will not be sent");
            logger.info("Would send rejection email to: {}", organizerEmail);
            logger.info("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(organizerEmail);
            message.setSubject("❌ Event Rejected: " + event.getTitle());
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your event has been REJECTED.\n\n" +
                "Event: %s\n" +
                "Reason: %s\n\n" +
                "You can modify and resubmit.\n\n" +
                "Best regards,\n" +
                "Event Management Team",
                event.getOrganizerName(),
                event.getTitle(),
                reason
            ));
            
            mailSender.send(message);
            logger.info("✅ Event rejection email sent successfully to: {}", organizerEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send rejection email to {}: {}", organizerEmail, e.getMessage());
        }
        logger.info("========================================");
    }
    
    public void sendEventReminderEmail(Event event, String participantEmail, String participantName) {
        logger.info("========================================");
        logger.info("📧 EVENT REMINDER EMAIL NOTIFICATION");
        logger.info("========================================");
        logger.info("Event: {}", event.getTitle());
        logger.info("To: {}", participantEmail);
        
        if (mailSender == null) {
            logger.warn("⚠️ Email not configured - Mail will not be sent");
            logger.info("Would send reminder email to: {}", participantEmail);
            logger.info("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(participantEmail);
            message.setSubject("🔔 Reminder: " + event.getTitle());
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Reminder: Event '%s' is coming up soon!\n\n" +
                "Date: %s\n" +
                "Venue: %s\n\n" +
                "Don't miss it!\n\n" +
                "Best regards,\n" +
                "Event Management Team",
                participantName,
                event.getTitle(),
                event.getEventDate() != null ? event.getEventDate().format(DATE_FORMATTER) : "N/A",
                event.getVenue()
            ));
            
            mailSender.send(message);
            logger.info("✅ Reminder email sent successfully to: {}", participantEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send reminder email to {}: {}", participantEmail, e.getMessage());
        }
        logger.info("========================================");
    }
}