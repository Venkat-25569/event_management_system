   package com.events.event_management.service;

import com.events.event_management.model.Club;
import com.events.event_management.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClubService {
    
    @Autowired
    private ClubRepository clubRepository;
    
    /**
     * Get all clubs
     */
    public List<Club> getAllClubs() {
        List<Club> clubs = clubRepository.findAll();
        System.out.println("Service: Found " + clubs.size() + " clubs");
        return clubs;
    }
    
    /**
     * Get club by ID
     */
    public Club getClubById(Long id) {
        Optional<Club> club = clubRepository.findById(id);
        if (club.isPresent()) {
            System.out.println("Service: Found club - " + club.get().getClubName());
            return club.get();
        }
        System.out.println("Service: Club not found with ID - " + id);
        return null;
    }
    
    /**
     * Create new club
     */
    public Club createClub(Club club) {
        System.out.println("Service: Creating club - " + club.getClubName());
        
        // Validate required fields
        if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
            throw new IllegalArgumentException("Club name is required");
        }
        
        // Save club
        Club savedClub = clubRepository.save(club);
        System.out.println("Service: Club saved successfully with ID - " + savedClub.getId());
        
        return savedClub;
    }
    
    /**
     * Update existing club
     */
    public Club updateClub(Club club) {
        System.out.println("Service: Updating club with ID - " + club.getId());
        
        // Check if club exists
        if (!clubRepository.existsById(club.getId())) {
            System.out.println("Service: Club not found for update - " + club.getId());
            return null;
        }
        
        // Update club
        Club updatedClub = clubRepository.save(club);
        System.out.println("Service: Club updated successfully - " + updatedClub.getClubName());
        
        return updatedClub;
    }
    
    /**
     * Delete club by ID
     */
    public boolean deleteClub(Long id) {
        System.out.println("Service: Deleting club with ID - " + id);
        
        if (clubRepository.existsById(id)) {
            clubRepository.deleteById(id);
            System.out.println("Service: Club deleted successfully");
            return true;
        }
        
        System.out.println("Service: Club not found for deletion - " + id);
        return false;
    }
    
    /**
     * Search clubs by name (optional)
     */
    public List<Club> searchClubsByName(String name) {
        return clubRepository.findByClubNameContainingIgnoreCase(name);
    }
    
    /**
     * Check if club name exists (optional)
     */
    public boolean clubNameExists(String clubName) {
        return clubRepository.existsByClubName(clubName);
    }
}