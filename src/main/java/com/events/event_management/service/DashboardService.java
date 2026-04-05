  package com.events.event_management.service;

import com.events.event_management.model.Event;
import com.events.event_management.model.EventRegistration;
import com.events.event_management.model.Attendance;
import com.events.event_management.repository.EventRepository;
import com.events.event_management.repository.EventRegistrationRepository;
import com.events.event_management.repository.AttendanceRepository;
import com.events.event_management.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private EventRegistrationRepository registrationRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    /**
     * ADMIN Dashboard Statistics
     */
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<Event> allEvents = eventRepository.findAll();
            
            stats.put("totalEvents", allEvents.size());
            stats.put("pendingEvents", allEvents.stream().filter(e -> "PENDING".equals(e.getStatus())).count());
            stats.put("approvedEvents", allEvents.stream().filter(e -> "APPROVED".equals(e.getStatus())).count());
            stats.put("rejectedEvents", allEvents.stream().filter(e -> "REJECTED".equals(e.getStatus())).count());
            stats.put("totalClubs", clubRepository.count());
            stats.put("totalRegistrations", registrationRepository.count());
            stats.put("totalAttendance", attendanceRepository.count());
            
            LocalDate today = LocalDate.now();
            LocalDate weekFromNow = today.plusDays(7);
            long upcomingEvents = allEvents.stream()
                .filter(e -> e.getEventDate() != null && 
                            e.getEventDate().isAfter(today) && 
                            e.getEventDate().isBefore(weekFromNow))
                .count();
            stats.put("upcomingEvents", upcomingEvents);
            
        } catch (Exception e) {
            System.err.println("Error in getAdminDashboardStats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * HOD Dashboard Statistics
     */
    public Map<String, Object> getHODDashboardStats(String department) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<Event> allEvents = eventRepository.findAll();
            List<Event> pendingEvents = allEvents.stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .collect(Collectors.toList());
            
            stats.put("pendingApprovals", pendingEvents.size());
            stats.put("approvedThisMonth", getEventsApprovedThisMonth(allEvents));
            stats.put("upcomingEvents", getUpcomingEvents(allEvents));
            stats.put("totalEventsThisSemester", allEvents.size());
            stats.put("averageAttendance", calculateAverageAttendance());
            stats.put("budgetUtilization", 78);
            
            LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
            long urgentApprovals = pendingEvents.stream()
                .filter(e -> e.getEventDate() != null && e.getEventDate().isBefore(threeDaysFromNow))
                .count();
            stats.put("urgentApprovals", urgentApprovals);
            
        } catch (Exception e) {
            System.err.println("Error in getHODDashboardStats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * EVENT_COORDINATOR Dashboard Statistics
     */
    public Map<String, Object> getCoordinatorDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            System.out.println("DashboardService: Getting coordinator stats");
            
            List<Event> allEvents = eventRepository.findAll();
            System.out.println("DashboardService: Found " + allEvents.size() + " total events");
            
            List<Event> approvedEvents = allEvents.stream()
                .filter(e -> "APPROVED".equals(e.getStatus()))
                .collect(Collectors.toList());
            
            long pendingCount = allEvents.stream()
                .filter(e -> "PENDING".equals(e.getStatus())).count();
            
            stats.put("pendingApprovals", pendingCount);
            stats.put("approvedEvents", approvedEvents.size());
            stats.put("upcomingEvents", getUpcomingEvents(allEvents));
            stats.put("activeRegistrations", registrationRepository.count());
            
            LocalDate today = LocalDate.now();
            long needsCoordination = approvedEvents.stream()
                .filter(e -> e.getEventDate() != null && e.getEventDate().isAfter(today))
                .count();
            stats.put("needsCoordination", needsCoordination);
            
            stats.put("registrationsToday", getRegistrationsToday());
            stats.put("averageRegistrationsPerEvent", calculateAvgRegistrationsPerEvent());
            
            System.out.println("DashboardService: Stats - " + stats);
            
        } catch (Exception e) {
            System.err.println("Error in getCoordinatorDashboardStats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * STUDENT Dashboard Statistics
     */
    public Map<String, Object> getStudentDashboardStats(String email) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<EventRegistration> registrations = registrationRepository.findByStudentEmail(email);
            List<Attendance> attendance = attendanceRepository.findByStudentEmail(email);
            
            stats.put("registeredEvents", registrations.size());
            stats.put("attendedEvents", attendance.size());
            stats.put("pendingPayments", registrations.stream()
                .filter(r -> "PENDING".equals(r.getPaymentStatus())).count());
            stats.put("certificatesAvailable", attendance.size());
            
        } catch (Exception e) {
            System.err.println("Error in getStudentDashboardStats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Get Pending Events for Approval
     */
    public List<Map<String, Object>> getPendingEventsForApproval() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            System.out.println("DashboardService: Getting pending events");
            
            List<Event> pendingEvents = eventRepository.findAll().stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .sorted(Comparator.comparing(Event::getEventDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
            
            System.out.println("DashboardService: Found " + pendingEvents.size() + " pending events");
            
            for (Event event : pendingEvents) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("id", event.getId());
                eventData.put("title", event.getTitle());
                eventData.put("eventDate", event.getEventDate());
                eventData.put("venue", event.getVenue());
                eventData.put("clubName", event.getClubName());
                eventData.put("organizerName", event.getOrganizerName());
                eventData.put("participantCount", event.getParticipantCount());
                eventData.put("description", event.getDescription());
                eventData.put("status", event.getStatus());
                
                if (event.getEventDate() != null) {
                    long daysUntilEvent = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(), event.getEventDate()
                    );
                    eventData.put("daysUntilEvent", daysUntilEvent);
                    eventData.put("urgent", daysUntilEvent <= 3);
                }
                
                result.add(eventData);
            }
            
            System.out.println("DashboardService: Returning " + result.size() + " pending events");
            
        } catch (Exception e) {
            System.err.println("Error in getPendingEventsForApproval: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Get Recent Activities
     */
    public List<Map<String, Object>> getRecentActivities(int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            List<Event> recentEvents = eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getId).reversed())
                .limit(limit)
                .collect(Collectors.toList());
            
            for (Event event : recentEvents) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "event");
                activity.put("action", event.getStatus());
                activity.put("title", event.getTitle());
                activity.put("timestamp", LocalDateTime.now());
                activities.add(activity);
            }
            
        } catch (Exception e) {
            System.err.println("Error in getRecentActivities: " + e.getMessage());
            e.printStackTrace();
        }
        
        return activities;
    }
    
    /**
     * Get Event Analytics
     */
    public Map<String, Object> getEventAnalytics(String period) {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            List<Event> allEvents = eventRepository.findAll();
            
            Map<String, Long> eventsByStatus = allEvents.stream()
                .collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));
            analytics.put("eventsByStatus", eventsByStatus);
            
            Map<String, Long> eventsByClub = allEvents.stream()
                .filter(e -> e.getClubName() != null)
                .collect(Collectors.groupingBy(Event::getClubName, Collectors.counting()));
            analytics.put("eventsByClub", eventsByClub);
            
            analytics.put("monthlyTrend", getMonthlyEventTrend());
            
        } catch (Exception e) {
            System.err.println("Error in getEventAnalytics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return analytics;
    }
    
    // Helper methods
    
    private long getEventsApprovedThisMonth(List<Event> events) {
        return events.stream()
            .filter(e -> "APPROVED".equals(e.getStatus()))
            .count();
    }
    
    private long getUpcomingEvents(List<Event> events) {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        return events.stream()
            .filter(e -> e.getEventDate() != null && 
                        e.getEventDate().isAfter(today) && 
                        e.getEventDate().isBefore(weekFromNow))
            .count();
    }
    
    private double calculateAverageAttendance() {
        try {
            List<Attendance> allAttendance = attendanceRepository.findAll();
            if (allAttendance.isEmpty()) return 0;
            
            Map<Long, Long> attendanceByEvent = allAttendance.stream()
                .collect(Collectors.groupingBy(Attendance::getEventId, Collectors.counting()));
            
            return attendanceByEvent.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long getRegistrationsToday() {
        try {
            return registrationRepository.count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private double calculateAvgRegistrationsPerEvent() {
        try {
            List<EventRegistration> allRegistrations = registrationRepository.findAll();
            if (allRegistrations.isEmpty()) return 0;
            
            Map<Long, Long> regsByEvent = allRegistrations.stream()
                .collect(Collectors.groupingBy(EventRegistration::getEventId, Collectors.counting()));
            
            return regsByEvent.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private List<Map<String, Object>> getMonthlyEventTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        return trend;
    }
}