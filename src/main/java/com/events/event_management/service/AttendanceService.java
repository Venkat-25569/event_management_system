  package com.events.event_management.service;

import com.events.event_management.model.Attendance;
import com.events.event_management.model.Event;
import com.events.event_management.repository.AttendanceRepository;
import com.events.event_management.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    // Mark attendance with optional photo
    public Map<String, Object> markAttendance(Long eventId, String studentName, 
                                               String studentEmail, String rollNumber, 
                                               String department, String checkInMethod,
                                               String photoPath) {
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
            response.put("message", "Attendance only allowed for approved events");
            return response;
        }
        
        // Check if already marked attendance
        if (attendanceRepository.existsByEventIdAndStudentEmail(eventId, studentEmail)) {
            response.put("success", false);
            response.put("message", "Attendance already marked for this event");
            return response;
        }
        
        // Create new attendance record
        Attendance attendance = new Attendance();
        attendance.setEventId(eventId);
        attendance.setStudentName(studentName);
        attendance.setStudentEmail(studentEmail);
        attendance.setRollNumber(rollNumber);
        attendance.setDepartment(department);
        attendance.setCheckInMethod(checkInMethod != null ? checkInMethod : "MANUAL");
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setPhotoPath(photoPath);
        
        Attendance saved = attendanceRepository.save(attendance);
        
        response.put("success", true);
        response.put("message", "Attendance marked successfully!");
        response.put("checkInTime", saved.getCheckInTime());
        response.put("eventTitle", event.getTitle());
        
        return response;
    }
    
    // Get all attendance for an event
    public List<Attendance> getEventAttendance(Long eventId) {
        return attendanceRepository.findByEventId(eventId);
    }
    
    // Get attendance count for event
    public long getAttendanceCount(Long eventId) {
        List<Attendance> attendanceList = attendanceRepository.findByEventId(eventId);
        return attendanceList.size();
    }
    
    // Get student's attendance history
    public List<Map<String, Object>> getStudentAttendance(String studentEmail) {
        List<Attendance> attendanceList = attendanceRepository.findByStudentEmail(studentEmail);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Attendance att : attendanceList) {
            Optional<Event> eventOpt = eventRepository.findById(att.getEventId());
            if (eventOpt.isPresent()) {
                Event event = eventOpt.get();
                Map<String, Object> record = new HashMap<>();
                record.put("id", att.getId());
                record.put("eventId", att.getEventId());
                record.put("eventTitle", event.getTitle());
                record.put("eventDate", event.getEventDate());
                record.put("venue", event.getVenue());
                record.put("checkInTime", att.getCheckInTime());
                record.put("checkInMethod", att.getCheckInMethod());
                record.put("photoPath", att.getPhotoPath());
                result.add(record);
            }
        }
        
        return result;
    }
    
    // Get attendance statistics
    public Map<String, Object> getAttendanceStats(Long eventId) {
        Map<String, Object> stats = new HashMap<>();
        
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            return stats;
        }
        
        Event event = eventOpt.get();
        List<Attendance> attendanceList = attendanceRepository.findByEventId(eventId);
        long actualAttendance = attendanceList.size();
        long expectedAttendance = event.getParticipantCount() != null ? 
            event.getParticipantCount() : 0;
        
        stats.put("eventId", eventId);
        stats.put("eventTitle", event.getTitle());
        stats.put("expectedParticipants", expectedAttendance);
        stats.put("actualAttendance", actualAttendance);
        stats.put("attendancePercentage", 
            expectedAttendance > 0 ? (actualAttendance * 100.0 / expectedAttendance) : 0);
        
        return stats;
    }
    
    // Delete attendance record
    public boolean deleteAttendance(Long attendanceId) {
        if (attendanceRepository.existsById(attendanceId)) {
            attendanceRepository.deleteById(attendanceId);
            return true;
        }
        return false;
    }
    
    // Get attendance by email (for student dashboard)
    public List<Attendance> getAttendanceByEmail(String email) {
        return attendanceRepository.findByStudentEmail(email);
    }
}