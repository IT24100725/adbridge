package com.example.adbridge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_requests")
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String clientName;

    @Column(nullable = false, length = 150)
    private String clientEmail;

    @Column(length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    private String assignedTo; // staff username/name
    private String bookingId; // optional link to a booking
    
    @Column(columnDefinition = "TEXT")
    private String attachments; // JSON string of file paths/names
    
    // Express booking workflow fields
    @Column(name = "express_action")
    private String expressAction; // "APPROVED" or "REJECTED"
    
    @Column(name = "express_completed_at")
    private LocalDateTime expressCompletedAt;
    
    @Column(name = "notification_sent")
    private Boolean notificationSent = false;
    
    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
    public enum Priority { LOW, MEDIUM, HIGH }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Express booking workflow getters and setters
    public String getExpressAction() { return expressAction; }
    public void setExpressAction(String expressAction) { this.expressAction = expressAction; }
    public LocalDateTime getExpressCompletedAt() { return expressCompletedAt; }
    public void setExpressCompletedAt(LocalDateTime expressCompletedAt) { this.expressCompletedAt = expressCompletedAt; }
    public Boolean getNotificationSent() { return notificationSent; }
    public void setNotificationSent(Boolean notificationSent) { this.notificationSent = notificationSent; }
    public LocalDateTime getNotificationSentAt() { return notificationSentAt; }
    public void setNotificationSentAt(LocalDateTime notificationSentAt) { this.notificationSentAt = notificationSentAt; }
}





