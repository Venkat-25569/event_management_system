 package com.events.event_management.service;

import com.events.event_management.model.Payment;
import com.events.event_management.model.EventRegistration;
import com.events.event_management.repository.PaymentRepository;
import com.events.event_management.repository.EventRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private EventRegistrationRepository registrationRepository;
    
    public Map<String, Object> processPayment(Long registrationId, String studentEmail,
                                               BigDecimal amount, String paymentMethod) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<EventRegistration> regOpt = registrationRepository.findById(registrationId);
        if (!regOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Registration not found");
            return response;
        }
        
        EventRegistration registration = regOpt.get();
        
        if (!registration.getStudentEmail().equals(studentEmail)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }
        
        Payment payment = new Payment();
        payment.setRegistrationId(registrationId);
        payment.setStudentEmail(studentEmail);
        payment.setEventId(registration.getEventId());
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId("TXN" + System.currentTimeMillis());
        payment.setStatus("SUCCESS");
        
        Payment saved = paymentRepository.save(payment);
        
        registration.setPaymentStatus("PAID");
        registration.setAmount(amount);
        registrationRepository.save(registration);
        
        response.put("success", true);
        response.put("message", "Payment successful");
        response.put("transactionId", saved.getTransactionId());
        response.put("paymentId", saved.getId());
        
        return response;
    }
    
    public List<Payment> getStudentPayments(String studentEmail) {
        return paymentRepository.findByStudentEmail(studentEmail);
    }
    
    public List<Map<String, Object>> getPaymentHistory(String studentEmail) {
        List<Payment> payments = paymentRepository.findByStudentEmail(studentEmail);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Payment payment : payments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", payment.getId());
            item.put("eventId", payment.getEventId());
            item.put("amount", payment.getAmount());
            item.put("paymentMethod", payment.getPaymentMethod());
            item.put("transactionId", payment.getTransactionId());
            item.put("paymentDate", payment.getPaymentDate());
            item.put("status", payment.getStatus());
            result.add(item);
        }
        
        return result;
    }
}