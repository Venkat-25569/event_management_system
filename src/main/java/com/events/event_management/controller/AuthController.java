 package com.events.event_management.controller;

import com.events.event_management.dto.LoginRequest;
import com.events.event_management.dto.RegisterRequest;
import com.events.event_management.model.User;
import com.events.event_management.repository.UserRepository;
import com.events.event_management.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * User Registration
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Registration attempt for email: " + registerRequest.getEmail());
            System.out.println("Requested role: " + registerRequest.getRole());
            
            // Validate role - MUST be one of these 4
            List<String> validRoles = Arrays.asList("STUDENT", "EVENT_COORDINATOR", "HOD", "ADMIN");
            if (registerRequest.getRole() == null || !validRoles.contains(registerRequest.getRole())) {
                System.err.println("Invalid role provided: " + registerRequest.getRole());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid role. Must be STUDENT, EVENT_COORDINATOR, HOD, or ADMIN");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Check if email already exists
            Optional<User> existingUserByEmail = userRepository.findByEmail(registerRequest.getEmail());
            if (existingUserByEmail.isPresent()) {
                System.err.println("Email already registered: " + registerRequest.getEmail());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Email already registered");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Check if username already exists
            Optional<User> existingUserByUsername = userRepository.findByUsername(registerRequest.getUsername());
            if (existingUserByUsername.isPresent()) {
                System.err.println("Username already taken: " + registerRequest.getUsername());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username already taken");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate password
            if (registerRequest.getPassword() == null || registerRequest.getPassword().length() < 6) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Create new user
            User user = new User();
            user.setFullName(registerRequest.getFullName());
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(registerRequest.getRole());
            
            // Optional fields
            if (registerRequest.getPhone() != null && !registerRequest.getPhone().isEmpty()) {
                user.setPhone(registerRequest.getPhone());
            }
            if (registerRequest.getDepartment() != null && !registerRequest.getDepartment().isEmpty()) {
                user.setDepartment(registerRequest.getDepartment());
            }
            
            // Save user
            User savedUser = userRepository.save(user);
            
            System.out.println("User registered successfully: " + savedUser.getEmail() + " with role: " + savedUser.getRole());
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("email", savedUser.getEmail());
            response.put("fullName", savedUser.getFullName());
            response.put("role", savedUser.getRole());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * User Login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for email: " + loginRequest.getEmail());
            
            // Find user by EMAIL (not username)
            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
            
            if (userOpt.isEmpty()) {
                System.err.println("User not found: " + loginRequest.getEmail());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            User user = userOpt.get();
            System.out.println("User found: " + user.getFullName() + " with role: " + user.getRole());
            
            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                System.err.println("Invalid password for user: " + loginRequest.getEmail());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            System.out.println("JWT token generated successfully for: " + user.getEmail());
            
            // Return successful login response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole());
            response.put("username", user.getUsername());
            
            System.out.println("Login successful for: " + user.getEmail() + " (" + user.getRole() + ")");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get current user info (optional - for profile page)
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from "Bearer <token>"
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            User user = userOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("phone", user.getPhone());
            response.put("department", user.getDepartment());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Logout (optional - just for logging purposes, client clears token)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // In JWT, logout is handled client-side by removing the token
        // This endpoint is just for logging purposes
        System.out.println("User logged out");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
}