 package com.events.event_management.repository;

import com.events.event_management.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Find events by status
    List<Event> findByStatus(String status);
    
    // Find events by club name
    List<Event> findByClubName(String clubName);
}