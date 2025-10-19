package com.example.adbridge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_audit_logs")
public class PaymentAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @Column(name = "action", nullable = false)
    private String action; // CONFIRM, CANCEL, REFUND, DELETE
    
    @Column(name = "performed_by", nullable = false)
    private String performedBy;
    
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;
    
    @Column(name = "old_status")
    private String oldStatus;
    
    @Column(name = "new_status")
    private String newStatus;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
    public PaymentAuditLog() {
        this.performedAt = LocalDateTime.now();
    }
    
    public PaymentAuditLog(Long paymentId, String action, String performedBy, String oldStatus, String newStatus, String reason) {
        this();
        this.paymentId = paymentId;
        this.action = action;
        this.performedBy = performedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
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
    
    public LocalDateTime getPerformedAt() {
        return performedAt;
    }
    
    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
    
    public String getOldStatus() {
        return oldStatus;
    }
    
    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}





