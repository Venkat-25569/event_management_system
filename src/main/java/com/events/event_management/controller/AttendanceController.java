  package com.events.event_management.controller;

import com.events.event_management.model.Attendance;
import com.events.event_management.service.AttendanceService;
import com.events.event_management.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private ExportService exportService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final String PHOTO_UPLOAD_DIR = "uploads/attendance-photos/";
    
    // ============================================
    // GET PARTICIPANTS FOR EVENT (FOR DROPDOWN) - FIXED!
    // ============================================
     
            // ============================================
// GET PARTICIPANTS FOR EVENT (FOR DROPDOWN) - FLEXIBLE VERSION
// ============================================
@GetMapping("/event/{eventId}/participants")
public ResponseEntity<?> getEventParticipants(@PathVariable Long eventId) {
    try {
        System.out.println("📡 Fetching participants for event: " + eventId);
        
        // Try to get column names dynamically
        String sql = "SELECT * FROM participants WHERE event_id = ? ORDER BY name LIMIT 1";
        
        try {
            // First, check if any participants exist
            List<Map<String, Object>> testQuery = jdbcTemplate.queryForList(sql, eventId);
            
            if (testQuery.isEmpty()) {
                System.out.println("⚠️ No participants found for event: " + eventId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("participants", new ArrayList<>());
                response.put("count", 0);
                
                return ResponseEntity.ok(response);
            }
            
            // Get actual column names from the first row
            Map<String, Object> firstRow = testQuery.get(0);
            System.out.println("📋 Available columns: " + firstRow.keySet());
            
            // Build query based on available columns
            StringBuilder selectSql = new StringBuilder("SELECT ");
            selectSql.append("id, ");
            selectSql.append("name, ");
            selectSql.append("email");
            
            // Add optional columns if they exist
            if (firstRow.containsKey("roll_number")) {
                selectSql.append(", roll_number");
            } else if (firstRow.containsKey("rollNumber")) {
                selectSql.append(", rollNumber as roll_number");
            } else if (firstRow.containsKey("student_id")) {
                selectSql.append(", student_id as roll_number");
            }
            
            if (firstRow.containsKey("department")) {
                selectSql.append(", department");
            }
            
            if (firstRow.containsKey("phone")) {
                selectSql.append(", phone");
            }
            
            selectSql.append(" FROM participants WHERE event_id = ? ORDER BY name");
            
            System.out.println("🔍 Query: " + selectSql.toString());
            
            List<Map<String, Object>> participants = jdbcTemplate.queryForList(selectSql.toString(), eventId);
            
            System.out.println("✅ Found " + participants.size() + " participants");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("participants", participants);
            response.put("count", participants.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error in SQL query: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Try with basic columns only
            System.out.println("🔄 Trying fallback query with basic columns...");
            
            String fallbackSql = "SELECT id, name, email FROM participants WHERE event_id = ? ORDER BY name";
            List<Map<String, Object>> participants = jdbcTemplate.queryForList(fallbackSql, eventId);
            
            System.out.println("✅ Fallback successful: Found " + participants.size() + " participants");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("participants", participants);
            response.put("count", participants.size());
            
            return ResponseEntity.ok(response);
        }
        
    } catch (Exception e) {
        System.err.println("❌ Error fetching participants: " + e.getMessage());
        e.printStackTrace();
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Failed to fetch participants: " + e.getMessage());
        error.put("error", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
    
    // ============================================
    // GET ALL ATTENDANCE RECORDS (FOR ADMIN PAGE)
    // ============================================
    @GetMapping("")
    public ResponseEntity<?> getAllAttendance(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String search) {
        try {
            System.out.println("📡 Fetching all attendance - Event: " + eventId + ", Search: " + search);
            
            List<Attendance> records;
            
            if (eventId != null) {
                records = attendanceService.getEventAttendance(eventId);
            } else {
                String sql = "SELECT a.*, e.title as event_name " +
                           "FROM attendance a " +
                           "JOIN events e ON a.event_id = e.id " +
                           "WHERE 1=1 ";
                
                List<Object> params = new ArrayList<>();
                
                if (search != null && !search.trim().isEmpty()) {
                    sql += "AND (a.student_name LIKE ? OR a.student_email LIKE ?) ";
                    String searchPattern = "%" + search + "%";
                    params.add(searchPattern);
                    params.add(searchPattern);
                }
                
                sql += "ORDER BY a.check_in_time DESC";
                
                List<Map<String, Object>> rawRecords = jdbcTemplate.queryForList(sql, params.toArray());
                
                System.out.println("✅ Found " + rawRecords.size() + " attendance records");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("records", rawRecords);
                response.put("count", rawRecords.size());
                
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            response.put("count", records.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching attendance: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch attendance records: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Mark attendance with photo (for QR scan)
    @PostMapping("/mark-with-photo")
    public ResponseEntity<?> markAttendanceWithPhoto(
            @RequestParam("eventId") Long eventId,
            @RequestParam("studentName") String studentName,
            @RequestParam("studentEmail") String studentEmail,
            @RequestParam(value = "rollNumber", required = false) String rollNumber,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam("checkInMethod") String checkInMethod,
            @RequestParam("photo") MultipartFile photo) {
        
        try {
            String photoPath = null;
            
            if (photo != null && !photo.isEmpty()) {
                File uploadDir = new File(PHOTO_UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                String originalFilename = photo.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
                String filename = UUID.randomUUID().toString() + extension;
                
                Path filepath = Paths.get(PHOTO_UPLOAD_DIR + filename);
                Files.write(filepath, photo.getBytes());
                
                photoPath = PHOTO_UPLOAD_DIR + filename;
            }
            
            Map<String, Object> result = attendanceService.markAttendance(
                eventId, studentName, studentEmail, rollNumber, department, checkInMethod, photoPath
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to mark attendance: " + e.getMessage()));
        }
    }
    
    // Mark attendance without photo (for manual entry)
    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestBody Map<String, String> request) {
        try {
            System.out.println("📥 Manual attendance request: " + request);
            
            Long eventId = Long.parseLong(request.get("eventId"));
            String studentName = request.get("studentName");
            String studentEmail = request.get("studentEmail");
            String rollNumber = request.get("rollNumber");
            String department = request.get("department");
            String checkInMethod = request.getOrDefault("checkInMethod", "MANUAL");
            
            Map<String, Object> result = attendanceService.markAttendance(
                eventId, studentName, studentEmail, rollNumber, department, checkInMethod, null
            );
            
            System.out.println("✅ Attendance marked successfully");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Error marking attendance: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to mark attendance: " + e.getMessage()));
        }
    }
    
    // Get all attendance for an event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventAttendance(@PathVariable Long eventId) {
        try {
            List<Attendance> attendance = attendanceService.getEventAttendance(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", attendance);
            response.put("count", attendance.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch attendance: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Get attendance statistics
    @GetMapping("/event/{eventId}/stats")
    public ResponseEntity<?> getAttendanceStats(@PathVariable Long eventId) {
        Map<String, Object> stats = attendanceService.getAttendanceStats(eventId);
        return ResponseEntity.ok(stats);
    }
    
    // Delete attendance record
    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<?> deleteAttendance(@PathVariable Long attendanceId) {
        boolean deleted = attendanceService.deleteAttendance(attendanceId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Attendance deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("success", false, "message", "Attendance record not found"));
    }
    
    // Export attendance to Excel
    @GetMapping("/event/{eventId}/export")
    public ResponseEntity<ByteArrayResource> exportAttendance(@PathVariable Long eventId) {
        try {
            List<Attendance> attendanceList = attendanceService.getEventAttendance(eventId);
            
            byte[] excelData = exportService.exportAttendanceToExcel(attendanceList, eventId);
            
            ByteArrayResource resource = new ByteArrayResource(excelData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=Attendance_Event_" + eventId + ".xlsx");
            
            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelData.length)
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/student/{email}")
    public ResponseEntity<?> getStudentAttendance(@PathVariable String email) {
        try {
            List<Attendance> attendanceList = attendanceService.getAttendanceByEmail(email);
            return ResponseEntity.ok(attendanceList);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to fetch attendance: " + e.getMessage()));
        }
    }
}