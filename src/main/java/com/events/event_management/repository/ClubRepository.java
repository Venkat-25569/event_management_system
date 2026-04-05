package com.events.event_management.repository;

import com.events.event_management.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    
    // Find clubs by name (optional - for search functionality)
    List<Club> findByClubNameContainingIgnoreCase(String clubName);
    
    // Check if club exists by name (optional - to prevent duplicates)
    boolean existsByClubName(String clubName);
}