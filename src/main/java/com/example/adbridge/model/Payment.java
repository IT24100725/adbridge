package com.example.adbridge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id", unique = true, nullable = false)
    private String paymentId;
    
    @Column(name = "booking_id", nullable = false)
    private String bookingId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
    
    @Column(name = "amount", nullable = false)
    private Integer amount;
    
    @Column(name = "currency", nullable = false)
    private String currency = "LKR";
    
    // Card payment fields
    @Column(name = "card_number")
    private String cardNumber;
    
    @Column(name = "cardholder_name")
    private String cardholderName;
    
    @Column(name = "expiry_date")
    private String expiryDate;
    
    @Column(name = "cvv")
    private String cvv;
    
    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;
    
    // Cash payment fields
    @Column(name = "preferred_date")
    private String preferredDate;
    
    @Column(name = "preferred_time")
    private String preferredTime;
    
    @Column(name = "payment_location")
    private String paymentLocation;
    
    @Column(name = "contact_number")
    private String contactNumber;
    
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
    
    // Bank transfer fields
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "transaction_reference")
    private String transactionReference;
    
    // Common fields
    @Column(name = "payment_notes", columnDefinition = "TEXT")
    private String paymentNotes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // External gateway fields
    @Column(name = "external_transaction_id")
    private String externalTransactionId;
    
    @Column(name = "external_status")
    private String externalStatus;
    
    @Column(name = "external_message", columnDefinition = "TEXT")
    private String externalMessage;
    
    // Soft delete and audit fields
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private String deletedBy;
    
    @Column(name = "last_action")
    private String lastAction;
    
    @Column(name = "last_action_by")
    private String lastActionBy;
    
    @Column(name = "last_action_at")
    private LocalDateTime lastActionAt;
    
    @Column(name = "action_reason", columnDefinition = "TEXT")
    private String actionReason;
    
    // Constructors
    public Payment() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Payment(String bookingId, PaymentMethod paymentMethod, PaymentStatus paymentStatus, Integer amount) {
        this();
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getCardholderName() {
        return cardholderName;
    }
    
    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }
    
    public String getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getCvv() {
        return cvv;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    
    public String getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public String getPreferredDate() {
        return preferredDate;
    }
    
    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }
    
    public String getPreferredTime() {
        return preferredTime;
    }
    
    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }
    
    public String getPaymentLocation() {
        return paymentLocation;
    }
    
    public void setPaymentLocation(String paymentLocation) {
        this.paymentLocation = paymentLocation;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public String getPaymentNotes() {
        return paymentNotes;
    }
    
    public void setPaymentNotes(String paymentNotes) {
        this.paymentNotes = paymentNotes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getExternalTransactionId() {
        return externalTransactionId;
    }
    
    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }
    
    public String getExternalStatus() {
        return externalStatus;
    }
    
    public void setExternalStatus(String externalStatus) {
        this.externalStatus = externalStatus;
    }
    
    public String getExternalMessage() {
        return externalMessage;
    }
    
    public void setExternalMessage(String externalMessage) {
        this.externalMessage = externalMessage;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public String getDeletedBy() {
        return deletedBy;
    }
    
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
    
    public String getLastAction() {
        return lastAction;
    }
    
    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }
    
    public String getLastActionBy() {
        return lastActionBy;
    }
    
    public void setLastActionBy(String lastActionBy) {
        this.lastActionBy = lastActionBy;
    }
    
    public LocalDateTime getLastActionAt() {
        return lastActionAt;
    }
    
    public void setLastActionAt(LocalDateTime lastActionAt) {
        this.lastActionAt = lastActionAt;
    }
    
    public String getActionReason() {
        return actionReason;
    }
    
    public void setActionReason(String actionReason) {
        this.actionReason = actionReason;
    }
    
    // Enum for Payment Method
    public enum PaymentMethod {
        CARD, CASH, BANK_TRANSFER
    }
    
    // Enum for Payment Status
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, PENDING_CASH, PENDING_BANK_TRANSFER, REFUNDED
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
