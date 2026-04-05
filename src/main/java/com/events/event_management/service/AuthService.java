package com.events.event_management.service;

import com.events.event_management.model.User;
import com.events.event_management.repository.UserRepository;
import com.events.event_management.security.JwtUtil;  // ✅ FIXED PATH
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Map<String, Object> register(User user) {
        Map<String, Object> response = new HashMap<>();
        
        // Check if username exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            response.put("success", false);
            response.put("error", "Username already exists");
            return response;
        }
        
        // Check if email exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            response.put("success", false);
            response.put("error", "Email already exists");
            return response;
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Save user
        User savedUser = userRepository.save(user);
        
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("userId", savedUser.getId());
        
        return response;
    }
    
    public Map<String, Object> login(String emailOrUsername, String password) {
        Map<String, Object> response = new HashMap<>();
        
        // Try to find by email first, then username
        Optional<User> userOpt = userRepository.findByEmail(emailOrUsername);
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByUsername(emailOrUsername);
        }
        
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("error", "Invalid credentials");
            return response;
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("error", "Invalid credentials");
            return response;
        }
        
        // Generate token with EMAIL (not username)
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        
        response.put("success", true);
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        
        return response;
    }
}