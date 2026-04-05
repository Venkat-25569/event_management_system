package com.events.event_management.controller;

import com.events.event_management.model.Club;
import com.events.event_management.model.Event;
import com.events.event_management.service.ClubService;
import com.events.event_management.service.EventService;
import com.events.event_management.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {
    
    @Autowired
    private ExportService exportService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private ClubService clubService;
    
    @GetMapping("/events/excel")
    public ResponseEntity<ByteArrayResource> exportEventsToExcel() {
        try {
            List<Event> events = eventService.getAllEvents();
            byte[] excelData = exportService.exportEventsToExcel(events);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "Events_Report_" + timestamp + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/clubs/excel")
    public ResponseEntity<ByteArrayResource> exportClubsToExcel() {
        try {
            List<Club> clubs = clubService.getAllClubs();
            byte[] excelData = exportService.exportClubsToExcel(clubs);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "Clubs_Report_" + timestamp + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}