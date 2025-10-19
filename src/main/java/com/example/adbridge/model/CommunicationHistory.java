package com.example.adbridge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "communication_history")
public class CommunicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunicationType type;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String subject;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Size(max = 100, message = "Staff member name must not exceed 100 characters")
    @Column(length = 100)
    private String staffMember;

    public enum CommunicationType {
        CALL, EMAIL, MEETING, SMS, OTHER
    }

    public CommunicationHistory() {}

    public CommunicationHistory(User user, CommunicationType type, String subject, String description, String staffMember) {
        this.user = user;
        this.type = type;
        this.subject = subject;
        this.description = description;
        this.staffMember = staffMember;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CommunicationType getType() { return type; }
    public void setType(CommunicationType type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStaffMember() { return staffMember; }
    public void setStaffMember(String staffMember) { this.staffMember = staffMember; }
}


