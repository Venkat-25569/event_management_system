  package com.events.event_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RubricsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ============================================
    // 1. CREATE RUBRIC
    // ============================================
    @PostMapping("/rubrics")
    @Transactional
    public ResponseEntity<?> createRubric(@RequestBody Map<String, Object> request) {
        try {
            Integer eventId = Integer.parseInt(request.get("event_id").toString());
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Integer totalPoints = Integer.parseInt(request.get("total_points").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> criteria = (List<Map<String, Object>>) request.get("criteria");

            // Insert rubric
            String rubricSql = "INSERT INTO rubrics (event_id, name, description, total_points, status, created_by) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(rubricSql, eventId, name, description, totalPoints, "draft", 1);

            // Get last inserted ID
            Integer rubricId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

            // Insert criteria
            if (criteria != null && !criteria.isEmpty()) {
                String criteriaSql = "INSERT INTO rubric_criteria (rubric_id, name, description, max_score, weightage, sort_order) VALUES (?, ?, ?, ?, ?, ?)";
                
                for (int i = 0; i < criteria.size(); i++) {
                    Map<String, Object> criterion = criteria.get(i);
                    jdbcTemplate.update(criteriaSql,
                        rubricId,
                        criterion.get("name"),
                        criterion.get("description"),
                        criterion.get("max_score"),
                        criterion.get("weightage"),
                        i + 1
                    );
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rubric created successfully");
            response.put("rubric_id", rubricId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to create rubric");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 2. GET RUBRICS BY EVENT
    // ============================================
    @GetMapping("/events/{event_id}/rubrics")
    public ResponseEntity<?> getRubricsByEvent(@PathVariable("event_id") int eventId) {
        try {
            String sql = "SELECT r.*, COUNT(rc.id) as criteria_count " +
                        "FROM rubrics r " +
                        "LEFT JOIN rubric_criteria rc ON r.id = rc.rubric_id " +
                        "WHERE r.event_id = ? " +
                        "GROUP BY r.id " +
                        "ORDER BY r.created_at DESC";

            List<Map<String, Object>> rubrics = jdbcTemplate.queryForList(sql, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rubrics", rubrics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch rubrics");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 3. GET RUBRIC DETAILS
    // ============================================
    @GetMapping("/rubrics/{rubric_id}")
    public ResponseEntity<?> getRubricDetails(@PathVariable("rubric_id") int rubricId) {
        try {
            String rubricSql = "SELECT * FROM rubrics WHERE id = ?";
            List<Map<String, Object>> rubricList = jdbcTemplate.queryForList(rubricSql, rubricId);

            if (rubricList.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Rubric not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            String criteriaSql = "SELECT * FROM rubric_criteria WHERE rubric_id = ? ORDER BY sort_order";
            List<Map<String, Object>> criteria = jdbcTemplate.queryForList(criteriaSql, rubricId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rubric", rubricList.get(0));
            response.put("criteria", criteria);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch rubric details");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 4. ADD PARTICIPANT - FIXED VERSION
    // ============================================
    @PostMapping("/participants")
    @Transactional
    public ResponseEntity<?> addParticipant(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("📥 Received participant data: " + request);
            
            // Get values with proper field names
            Integer eventId = request.get("event_id") != null ? Integer.parseInt(request.get("event_id").toString()) : null;
            
            // Handle both 'type' and 'participant_type' field names
            String participantType = request.get("type") != null ? 
                (String) request.get("type") : 
                (String) request.get("participant_type");
            
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String studentId = (String) request.get("student_id");
            String teamName = (String) request.get("team_name");
            String phone = (String) request.get("phone");
            String registrationNumber = (String) request.get("registration_number");
            
            // Validate required fields
            if (eventId == null || participantType == null || name == null || email == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Missing required fields: event_id, type, name, email");
                return ResponseEntity.badRequest().body(error);
            }
            
            String sql = "INSERT INTO participants " +
                        "(event_id, participant_type, name, team_name, student_id, registration_number, email, phone, status, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

            jdbcTemplate.update(sql,
                eventId,
                participantType,
                name,
                teamName,
                studentId,
                registrationNumber,
                email,
                phone,
                "confirmed"
            );

            Integer participantId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            
            System.out.println("✅ Participant added successfully! ID: " + participantId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Participant added successfully");
            response.put("participant_id", participantId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Error adding participant: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to add participant: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 4B. DELETE PARTICIPANT - NEW
    // ============================================
    @DeleteMapping("/participants/{id}")
    public ResponseEntity<?> deleteParticipant(@PathVariable Integer id) {
        try {
            System.out.println("🗑️ Deleting participant ID: " + id);
            
            String sql = "DELETE FROM participants WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, id);
            
            if (rowsAffected == 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Participant not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            System.out.println("✅ Participant deleted successfully!");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Participant deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting participant: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error deleting participant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 5. GET PARTICIPANTS
    // ============================================
    @GetMapping("/events/{event_id}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable("event_id") int eventId) {
        try {
            String sql = "SELECT p.*, GROUP_CONCAT(tm.name SEPARATOR ', ') as team_member_names " +
                        "FROM participants p " +
                        "LEFT JOIN team_members tm ON p.id = tm.participant_id " +
                        "WHERE p.event_id = ? " +
                        "GROUP BY p.id " +
                        "ORDER BY p.created_at DESC";

            List<Map<String, Object>> participants = jdbcTemplate.queryForList(sql, eventId);
            
            // Transform participant_type to type for frontend compatibility
            for (Map<String, Object> participant : participants) {
                if (participant.containsKey("participant_type")) {
                    participant.put("type", participant.get("participant_type"));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("participants", participants);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch participants");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 6. ADD JUDGE
    // ============================================
    @PostMapping("/judges")
    public ResponseEntity<?> addJudge(@RequestBody Map<String, Object> request) {
        try {
            String accessCode = "JUDGE" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            String sql = "INSERT INTO judges " +
                        "(event_id, name, email, phone, designation, organization, expertise, status, access_code, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

            jdbcTemplate.update(sql,
                request.get("event_id"),
                request.get("name"),
                request.get("email"),
                request.get("phone"),
                request.get("designation"),
                request.get("organization"),
                request.get("expertise"),
                "invited",
                accessCode
            );

            Integer judgeId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Judge added successfully");
            response.put("judge_id", judgeId);
            response.put("access_code", accessCode);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to add judge");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 6B. DELETE JUDGE - NEW
    // ============================================
    @DeleteMapping("/judges/{id}")
    public ResponseEntity<?> deleteJudge(@PathVariable Integer id) {
        try {
            System.out.println("🗑️ Deleting judge ID: " + id);
            
            String sql = "DELETE FROM judges WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, id);
            
            if (rowsAffected == 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Judge not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            System.out.println("✅ Judge deleted successfully!");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Judge deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting judge: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error deleting judge: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ============================================
    // 7. GET JUDGES
    // ============================================
    @GetMapping("/events/{event_id}/judges")
    public ResponseEntity<?> getJudges(@PathVariable("event_id") int eventId) {
        try {
            String sql = "SELECT * FROM judges WHERE event_id = ? ORDER BY created_at DESC";
            List<Map<String, Object>> judges = jdbcTemplate.queryForList(sql, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("judges", judges);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch judges");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

      // ============================================
// 8. SUBMIT SCORES - FIXED VERSION
// ============================================
@PostMapping("/scores")
@Transactional
public ResponseEntity<?> submitScores(@RequestBody Map<String, Object> request) {
    try {
        System.out.println("📥 Received score submission: " + request);
        
        Integer judgeId = Integer.parseInt(request.get("judge_id").toString());
        Integer participantId = Integer.parseInt(request.get("participant_id").toString());
        Integer rubricId = Integer.parseInt(request.get("rubric_id").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> scores = (List<Map<String, Object>>) request.get("scores");

        System.out.println("📊 Judge: " + judgeId + ", Participant: " + participantId + ", Rubric: " + rubricId);
        System.out.println("📊 Scores count: " + scores.size());

        // Delete existing scores for this combination
        String deleteSql = "DELETE FROM scores WHERE judge_id = ? AND participant_id = ? AND rubric_id = ?";
        int deleted = jdbcTemplate.update(deleteSql, judgeId, participantId, rubricId);
        System.out.println("🗑️ Deleted " + deleted + " existing scores");

        // Insert new scores (scored_at and updated_at are auto-generated)
        String insertSql = "INSERT INTO scores (judge_id, participant_id, rubric_id, criterion_id, score, comments) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        
        for (Map<String, Object> score : scores) {
            Integer criterionId = Integer.parseInt(score.get("criterion_id").toString());
            Double scoreValue = Double.parseDouble(score.get("score").toString());
            String comments = score.get("comments") != null ? score.get("comments").toString() : "";
            
            System.out.println("  📝 Inserting score - Criterion: " + criterionId + ", Score: " + scoreValue);
            
            jdbcTemplate.update(insertSql,
                judgeId,
                participantId,
                rubricId,
                criterionId,
                scoreValue,
                comments
            );
        }

        System.out.println("✅ All scores inserted successfully!");

        // Calculate final scores
        try {
            System.out.println("🧮 Calling CalculateFinalScores for rubric: " + rubricId);
            jdbcTemplate.update("CALL CalculateFinalScores(?)", rubricId);
            System.out.println("✅ Final scores calculated!");
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not calculate final scores: " + e.getMessage());
            // Don't fail the whole operation if this fails
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Scores submitted successfully");

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        System.err.println("❌ Error submitting scores: " + e.getMessage());
        e.printStackTrace();
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Failed to submit scores: " + e.getMessage());
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

        

    // ============================================
    // 9. GET SCOREBOARD
    // ============================================
    @GetMapping("/events/{event_id}/rubrics/{rubric_id}/scoreboard")
    public ResponseEntity<?> getScoreboard(
            @PathVariable("event_id") int eventId,
            @PathVariable("rubric_id") int rubricId) {
        try {
            // Get scoreboard
            String scoreboardSql = "SELECT " +
                    "fs.participant_id, p.name as participant_name, p.team_name, p.participant_type, " +
                    "fs.total_score, fs.weighted_score, fs.average_score, fs.rank, fs.judges_count, " +
                    "p.email as participant_email " +
                    "FROM final_scores fs " +
                    "JOIN participants p ON fs.participant_id = p.id " +
                    "WHERE fs.rubric_id = ? AND p.event_id = ? " +
                    "ORDER BY fs.rank ASC";
            List<Map<String, Object>> scoreboard = jdbcTemplate.queryForList(scoreboardSql, rubricId, eventId);

            // Get detailed scores
            String detailsSql = "SELECT " +
                    "s.participant_id, rc.id as criterion_id, rc.name as criterion_name, " +
                    "rc.max_score, AVG(s.score) as avg_score, COUNT(DISTINCT s.judge_id) as judge_count " +
                    "FROM scores s " +
                    "JOIN rubric_criteria rc ON s.criterion_id = rc.id " +
                    "WHERE s.rubric_id = ? " +
                    "GROUP BY s.participant_id, rc.id " +
                    "ORDER BY s.participant_id, rc.sort_order";
            List<Map<String, Object>> detailedScores = jdbcTemplate.queryForList(detailsSql, rubricId);

            // Organize scores by participant
            Map<Integer, List<Map<String, Object>>> scoresByParticipant = new HashMap<>();
            for (Map<String, Object> score : detailedScores) {
                Integer participantId = Integer.parseInt(score.get("participant_id").toString());
                scoresByParticipant.computeIfAbsent(participantId, k -> new ArrayList<>()).add(score);
            }

            // Combine scoreboard with detailed scores
            for (Map<String, Object> entry : scoreboard) {
                Integer participantId = Integer.parseInt(entry.get("participant_id").toString());
                entry.put("criteria_scores", scoresByParticipant.getOrDefault(participantId, new ArrayList<>()));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scoreboard", scoreboard);
            
            Map<String, Object> settings = new HashMap<>();
            settings.put("is_live", true);
            settings.put("show_scores", true);
            response.put("settings", settings);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch scoreboard");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}