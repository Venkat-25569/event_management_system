   package com.events.event_management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clubs")
public class Club {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "club_name", nullable = false, length = 255)
    private String clubName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "advisor_name", length = 255)
    private String advisorName;
    
    @Column(name = "contact_email", length = 255)
    private String contactEmail;
    
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    // Constructor
    public Club() {
        this.createdAt = java.time.LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getClubName() {
        return clubName;
    }
    
    public void setClubName(String clubName) {
        this.clubName = clubName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAdvisorName() {
        return advisorName;
    }
    
    public void setAdvisorName(String advisorName) {
        this.advisorName = advisorName;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return "Club{" +
                "id=" + id +
                ", clubName='" + clubName + '\'' +
                ", description='" + description + '\'' +
                ", advisorName='" + advisorName + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}