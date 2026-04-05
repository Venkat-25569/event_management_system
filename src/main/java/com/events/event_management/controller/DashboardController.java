  package com.events.event_management.controller;

import com.events.event_management.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/coordinator/stats")
    @PreAuthorize("hasAnyAuthority('EVENT_COORDINATOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCoordinatorStats() {
        try {
            System.out.println("DashboardController: getCoordinatorStats called");
            Map<String, Object> stats = dashboardService.getCoordinatorDashboardStats();
            System.out.println("DashboardController: Returning stats - " + stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("DashboardController Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/pending-events")
    @PreAuthorize("hasAnyAuthority('HOD', 'EVENT_COORDINATOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingEvents() {
        try {
            System.out.println("DashboardController: getPendingEvents called");
            List<Map<String, Object>> events = dashboardService.getPendingEventsForApproval();
            System.out.println("DashboardController: Returning " + events.size() + " pending events");
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            System.err.println("DashboardController Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    @GetMapping("/hod/stats")
    @PreAuthorize("hasAnyAuthority('HOD', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getHODStats(@RequestParam(required = false) String department) {
        try {
            Map<String, Object> stats = dashboardService.getHODDashboardStats(department);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        try {
            Map<String, Object> stats = dashboardService.getAdminDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}