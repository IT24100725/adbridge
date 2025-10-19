package com.example.adbridge.service;

import com.example.adbridge.repo.PaymentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentIdService {
    
    private final PaymentRepository paymentRepository;
    
    public PaymentIdService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Generate a unique payment ID based on booking ID
     * Format: PAY-{bookingId}
     * Example: PAY-ADB-20250921-154746-4482
     */
    public String generatePaymentId(String bookingId) {
        // Extract the booking ID part (remove any existing PAY- prefix if present)
        String cleanBookingId = bookingId;
        if (bookingId.startsWith("PAY-")) {
            cleanBookingId = bookingId.substring(4); // Remove "PAY-" prefix
        }
        
        // Generate payment ID with PAY- prefix
        String paymentId = "PAY-" + cleanBookingId;
        
        // Ensure uniqueness by checking if payment ID already exists
        int counter = 1;
        String originalPaymentId = paymentId;
        
        while (paymentRepository.existsByPaymentId(paymentId)) {
            paymentId = originalPaymentId + "-" + counter;
            counter++;
        }
        
        return paymentId;
    }
    
    /**
     * Generate a payment ID with timestamp suffix for extra uniqueness
     * Format: PAY-{bookingId}-{timestamp}
     */
    public String generatePaymentIdWithTimestamp(String bookingId) {
        String cleanBookingId = bookingId;
        if (bookingId.startsWith("PAY-")) {
            cleanBookingId = bookingId.substring(4);
        }
        
        // Add timestamp for extra uniqueness
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String paymentId = "PAY-" + cleanBookingId + "-" + timestamp;
        
        // Ensure uniqueness
        int counter = 1;
        String originalPaymentId = paymentId;
        
        while (paymentRepository.existsByPaymentId(paymentId)) {
            paymentId = originalPaymentId + "-" + counter;
            counter++;
        }
        
        return paymentId;
    }
    
    /**
     * Validate payment ID format
     */
    public boolean isValidPaymentId(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return false;
        }
        
        // Check if it starts with PAY- and has proper format
        return paymentId.startsWith("PAY-") && paymentId.length() > 4;
    }
    
    /**
     * Extract booking ID from payment ID
     * Example: PAY-ADB-20250921-154746-4482 -> ADB-20250921-154746-4482
     */
    public String extractBookingIdFromPaymentId(String paymentId) {
        if (paymentId == null || !paymentId.startsWith("PAY-")) {
            return paymentId;
        }
        
        return paymentId.substring(4); // Remove "PAY-" prefix
    }
}
