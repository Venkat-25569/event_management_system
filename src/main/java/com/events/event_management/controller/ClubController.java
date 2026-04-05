  package com.events.event_management.controller;

import com.events.event_management.model.Club;
import com.events.event_management.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
@CrossOrigin(origins = "*")
public class ClubController {
    
    @Autowired
    private ClubService clubService;
    
    /**
     * GET /api/clubs - Get all clubs
     */
    @GetMapping
    public ResponseEntity<List<Club>> getAllClubs() {
        try {
            System.out.println("Controller: GET /api/clubs - Fetching all clubs");
            List<Club> clubs = clubService.getAllClubs();
            System.out.println("Controller: Returning " + clubs.size() + " clubs");
            return ResponseEntity.ok(clubs);
        } catch (Exception e) {
            System.err.println("Controller: Error fetching clubs - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/clubs/{id} - Get club by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClubById(@PathVariable Long id) {
        try {
            System.out.println("Controller: GET /api/clubs/" + id);
            Club club = clubService.getClubById(id);
            
            if (club == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Club not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(club);
        } catch (Exception e) {
            System.err.println("Controller: Error fetching club - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/clubs - Create new club
     */
    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody Club club) {
        try {
            System.out.println("Controller: POST /api/clubs - Creating club");
            System.out.println("Controller: Received data - " + club.toString());
            
            // Validate club name
            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Club name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Club savedClub = clubService.createClub(club);
            System.out.println("Controller: Club created successfully - ID: " + savedClub.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClub);
        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Validation error - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("Controller: Error creating club - " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * PUT /api/clubs/{id} - Update existing club
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClub(@PathVariable Long id, @RequestBody Club club) {
        try {
            System.out.println("Controller: PUT /api/clubs/" + id);
            club.setId(id);
            
            Club updatedClub = clubService.updateClub(club);
            
            if (updatedClub == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Club not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            System.out.println("Controller: Club updated successfully");
            return ResponseEntity.ok(updatedClub);
        } catch (Exception e) {
            System.err.println("Controller: Error updating club - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * DELETE /api/clubs/{id} - Delete club
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClub(@PathVariable Long id) {
        try {
            System.out.println("Controller: DELETE /api/clubs/" + id);
            boolean deleted = clubService.deleteClub(id);
            
            if (!deleted) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Club not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Club deleted successfully");
            System.out.println("Controller: Club deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Controller: Error deleting club - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/clubs/search?name={name} - Search clubs by name (optional)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Club>> searchClubs(@RequestParam String name) {
        try {
            System.out.println("Controller: GET /api/clubs/search?name=" + name);
            List<Club> clubs = clubService.searchClubsByName(name);
            return ResponseEntity.ok(clubs);
        } catch (Exception e) {
            System.err.println("Controller: Error searching clubs - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}