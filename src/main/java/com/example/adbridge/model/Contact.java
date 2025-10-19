package com.example.adbridge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Column(nullable = false)
    private String email;
    
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be less than 200 characters")
    @Column(nullable = false)
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must be less than 2000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "is_replied", nullable = false)
    private Boolean isReplied = false;
    
    // New fields for structured contact data
    @Size(max = 50, message = "Request type must be less than 50 characters")
    @Column(name = "request_type")
    private String requestType;
    
    @Size(max = 50, message = "Booking ID must be less than 50 characters")
    @Column(name = "booking_id")
    private String bookingId;
    
    @Size(max = 100, message = "Client name must be less than 100 characters")
    @Column(name = "client_name")
    private String clientName;
    
    @Size(max = 100, message = "Client email must be less than 100 characters")
    @Column(name = "client_email")
    private String clientEmail;
    
    @Size(max = 200, message = "Client company must be less than 200 characters")
    @Column(name = "client_company")
    private String clientCompany;
    
    @Size(max = 20, message = "Client phone must be less than 20 characters")
    @Column(name = "client_phone")
    private String clientPhone;
    
    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;
    
    // Track if a support ticket has been created for this contact request
    @Column(name = "ticket_created")
    private Boolean ticketCreated = false;
    
    // Link to the user who made the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private User requestedByUser;
    
    // Constructors
    public Contact() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Contact(String name, String email, String phone, String subject, String message) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public Boolean getIsReplied() {
        return isReplied;
    }
    
    public void setIsReplied(Boolean isReplied) {
        this.isReplied = isReplied;
    }
    
    public User getRequestedByUser() {
        return requestedByUser;
    }
    
    public void setRequestedByUser(User requestedByUser) {
        this.requestedByUser = requestedByUser;
    }
    
    public String getRequestType() {
        return requestType;
    }
    
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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
    
    public String getClientCompany() {
        return clientCompany;
    }
    
    public void setClientCompany(String clientCompany) {
        this.clientCompany = clientCompany;
    }
    
    public String getClientPhone() {
        return clientPhone;
    }
    
    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }
    
    public String getRequestReason() {
        return requestReason;
    }
    
    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }
    
    public Boolean getTicketCreated() {
        return ticketCreated;
    }
    
    public void setTicketCreated(Boolean ticketCreated) {
        this.ticketCreated = ticketCreated;
    }
    
    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", isRead=" + isRead +
                ", isReplied=" + isReplied +
                '}';
    }
}

