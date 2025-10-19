package com.example.adbridge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        })
public class User {

    @Id
    @Column(length = 10)
    private String userId;

    @NotBlank(message = "Full name is required")
    @Size(max = 80, message = "Full name must not exceed 80 characters")
    @Column(nullable = false, length = 80)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(nullable = false, length = 20)
    private String phone;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    @Column(length = 100)
    private String companyName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Column(nullable = false, length = 30, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(nullable = false, length = 120)
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ClientStatus status = ClientStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Size(max = 1000, message = "Internal notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String internalNotes;
    
    // Link to the contact request that created this user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_from_contact_id")
    private Contact createdFromContact;

    public OffsetDateTime getCreatedAt() {
        return null;
    }

    public enum ClientStatus {
        ACTIVE, INACTIVE, VIP, SUSPENDED
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }

    public User() {}

    public User(String userId, String fullName, String email, String phone, String companyName, String username, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.companyName = companyName;
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public ClientStatus getStatus() { return status; }
    public void setStatus(ClientStatus status) { this.status = status; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
    
    public Contact getCreatedFromContact() { return createdFromContact; }
    public void setCreatedFromContact(Contact createdFromContact) { this.createdFromContact = createdFromContact; }
}