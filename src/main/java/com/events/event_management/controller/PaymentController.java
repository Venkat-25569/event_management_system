 package com.events.event_management.controller;

import com.events.event_management.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> request) {
        Long registrationId = Long.valueOf(request.get("registrationId").toString());
        String studentEmail = request.get("studentEmail").toString();
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String paymentMethod = request.get("paymentMethod").toString();
        
        Map<String, Object> response = paymentService.processPayment(
            registrationId, studentEmail, amount, paymentMethod
        );
        
        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/student/{email}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable String email) {
        List<Map<String, Object>> payments = paymentService.getPaymentHistory(email);
        return ResponseEntity.ok(payments);
    }
}