package com.example.adbridge.service;

import com.example.adbridge.model.Payment;
import com.example.adbridge.model.PaymentAuditLog;
import com.example.adbridge.repo.PaymentAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentAuditService.class);
    
    private final PaymentAuditLogRepository auditRepository;
    
    public PaymentAuditService(PaymentAuditLogRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    public void logAction(Payment payment, String action, String performedBy, String oldStatus, String newStatus, String reason) {
        try {
            PaymentAuditLog auditLog = new PaymentAuditLog(
                payment.getId(),
                action,
                performedBy,
                oldStatus,
                newStatus,
                reason
            );
            auditRepository.save(auditLog);
            
            // Update payment audit fields
            payment.setLastAction(action);
            payment.setLastActionBy(performedBy);
            payment.setLastActionAt(LocalDateTime.now());
            payment.setActionReason(reason);
            
            logger.info("Payment audit log created: Payment {} - Action {} by {}", 
                       payment.getPaymentId(), action, performedBy);
        } catch (Exception e) {
            logger.error("Failed to create payment audit log for payment {}: {}", 
                        payment.getPaymentId(), e.getMessage());
        }
    }
    
    public List<PaymentAuditLog> getPaymentHistory(Long paymentId) {
        return auditRepository.findByPaymentIdOrderByPerformedAtDesc(paymentId);
    }
    
    public void logConfirmation(Payment payment, String performedBy) {
        logAction(payment, "CONFIRM", performedBy, 
                 payment.getPaymentStatus().toString(), "COMPLETED", 
                 "Payment confirmed by " + performedBy);
    }
    
    public void logCancellation(Payment payment, String performedBy, String reason) {
        logAction(payment, "CANCEL", performedBy, 
                 payment.getPaymentStatus().toString(), "CANCELLED", reason);
    }
    
    public void logRefund(Payment payment, String performedBy, String reason) {
        logAction(payment, "REFUND", performedBy, 
                 payment.getPaymentStatus().toString(), "REFUNDED", reason);
    }
    
    public void logDeletion(Payment payment, String performedBy, String reason) {
        logAction(payment, "DELETE", performedBy, 
                 payment.getPaymentStatus().toString(), "DELETED", reason);
    }
}





