package com.example.adbridge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "express_booking_actions")
public class ExpressBookingAction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id", nullable = false)
    private String bookingId;
    
    @Column(name = "action", nullable = false)
    private String action; // "APPROVED" or "REJECTED"
    
    @Column(name = "performed_by")
    private String performedBy; // Admin name/role
    
    @Column(name = "client_name")
    private String clientName;
    
    @Column(name = "client_email")
    private String clientEmail;
    
    @Column(name = "total_amount")
    private Double totalAmount;
    
    @Column(name = "service_type")
    private String serviceType;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Constructors
    public ExpressBookingAction() {}
    
    public ExpressBookingAction(String bookingId, String action, String performedBy, 
                                String clientName, String clientEmail, Double totalAmount, String serviceType) {
        this.bookingId = bookingId;
        this.action = action;
        this.performedBy = performedBy;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.totalAmount = totalAmount;
        this.serviceType = serviceType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

