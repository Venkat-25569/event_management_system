 package com.events.event_management.repository;

import com.events.event_management.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    
    List<EventRegistration> findByStudentEmail(String studentEmail);
    
    List<EventRegistration> findByEventId(Long eventId);
    
    Optional<EventRegistration> findByEventIdAndStudentEmail(Long eventId, String studentEmail);
    
    long countByEventId(Long eventId);
    
    boolean existsByEventIdAndStudentEmail(Long eventId, String studentEmail);
}