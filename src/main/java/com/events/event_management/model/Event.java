 package com.events.event_management.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "event_date")
    private LocalDate eventDate;
    
    @Column(name = "venue")
    private String venue;
    
    @Column(name = "club_name")
    private String clubName;
    
    @Column(name = "organizer_name")
    private String organizerName;
    
    @Column(name = "participant_count")
    private Integer participantCount;
    
    @Column(name = "status")
    private String status = "PENDING";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // ✅ FILE UPLOAD FIELDS (for FileUploadController)
    @Column(name = "report_path")
    private String reportPath;
    
    @Column(name = "poster_path")
    private String posterPath;
    
    @Column(name = "photo_path")
    private String photoPath;
    
    // ✅ DUAL APPROVAL FIELDS
    @Column(name = "hod_approved")
    private Boolean hodApproved = false;
    
    @Column(name = "hod_approved_by")
    private String hodApprovedBy;
    
    @Column(name = "hod_approved_at")
    private LocalDateTime hodApprovedAt;
    
    @Column(name = "coordinator_approved")
    private Boolean coordinatorApproved = false;
    
    @Column(name = "coordinator_approved_by")
    private String coordinatorApprovedBy;
    
    @Column(name = "coordinator_approved_at")
    private LocalDateTime coordinatorApprovedAt;
    
    // Constructors
    public Event() {
        this.createdAt = LocalDateTime.now();
        this.hodApproved = false;
        this.coordinatorApproved = false;
        this.status = "PENDING";
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (hodApproved == null) {
            hodApproved = false;
        }
        if (coordinatorApproved == null) {
            coordinatorApproved = false;
        }
        if (status == null) {
            status = "PENDING";
        }
    }
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }
    
    public String getVenue() {
        return venue;
    }
    
    public void setVenue(String venue) {
        this.venue = venue;
    }
    
    public String getClubName() {
        return clubName;
    }
    
    public void setClubName(String clubName) {
        this.clubName = clubName;
    }
    
    public String getOrganizerName() {
        return organizerName;
    }
    
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // ✅ FILE UPLOAD GETTERS/SETTERS
    
    public String getReportPath() {
        return reportPath;
    }
    
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }
    
    public String getPosterPath() {
        return posterPath;
    }
    
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    // ✅ DUAL APPROVAL GETTERS/SETTERS
    
    public Boolean getHodApproved() {
        return hodApproved;
    }
    
    public void setHodApproved(Boolean hodApproved) {
        this.hodApproved = hodApproved;
    }
    
    public String getHodApprovedBy() {
        return hodApprovedBy;
    }
    
    public void setHodApprovedBy(String hodApprovedBy) {
        this.hodApprovedBy = hodApprovedBy;
    }
    
    public LocalDateTime getHodApprovedAt() {
        return hodApprovedAt;
    }
    
    public void setHodApprovedAt(LocalDateTime hodApprovedAt) {
        this.hodApprovedAt = hodApprovedAt;
    }
    
    public Boolean getCoordinatorApproved() {
        return coordinatorApproved;
    }
    
    public void setCoordinatorApproved(Boolean coordinatorApproved) {
        this.coordinatorApproved = coordinatorApproved;
    }
    
    public String getCoordinatorApprovedBy() {
        return coordinatorApprovedBy;
    }
    
    public void setCoordinatorApprovedBy(String coordinatorApprovedBy) {
        this.coordinatorApprovedBy = coordinatorApprovedBy;
    }
    
    public LocalDateTime getCoordinatorApprovedAt() {
        return coordinatorApprovedAt;
    }
    
    public void setCoordinatorApprovedAt(LocalDateTime coordinatorApprovedAt) {
        this.coordinatorApprovedAt = coordinatorApprovedAt;
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", eventDate=" + eventDate +
                ", venue='" + venue + '\'' +
                ", status='" + status + '\'' +
                ", hodApproved=" + hodApproved +
                ", coordinatorApproved=" + coordinatorApproved +
                '}';
    }
}