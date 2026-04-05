 package com.events.event_management.repository;

import com.events.event_management.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByStudentEmail(String studentEmail);
    
    List<Payment> findByEventId(Long eventId);
    
    List<Payment> findByRegistrationId(Long registrationId);
}