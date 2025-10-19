package com.example.adbridge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_actions")
public class CancellationAction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bookingId;
    private String action; // APPROVED or REJECTED
    private String performedBy;
    private String clientName;
    private String clientEmail;
    private Double totalAmount;
    private String serviceType;
    private String cancellationReason;
    private LocalDateTime createdAt;
    
    public CancellationAction() {
        this.createdAt = LocalDateTime.now();
    }
    
    public CancellationAction(String bookingId, String action, String performedBy, 
                            String clientName, String clientEmail, Double totalAmount, 
                            String serviceType, String cancellationReason) {
        this.bookingId = bookingId;
        this.action = action;
        this.performedBy = performedBy;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.totalAmount = totalAmount;
        this.serviceType = serviceType;
        this.cancellationReason = cancellationReason;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getPerformedBy() {
        return performedBy;
    }
    
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    
    public String getClientName() {
        return clientName;
    }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public String getClientEmail() {
        return clientEmail;
    }
    
    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
