 package com.events.event_management.repository;

import com.events.event_management.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByEventId(Long eventId);
    
    List<Attendance> findByStudentEmail(String studentEmail);  // ADD THIS LINE
    
    boolean existsByEventIdAndStudentEmail(Long eventId, String studentEmail);
}