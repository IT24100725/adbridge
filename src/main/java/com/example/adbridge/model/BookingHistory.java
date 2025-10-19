package com.example.adbridge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_history")
public class BookingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "previous_data", columnDefinition = "NVARCHAR(MAX)")
    private String previousData;

    @Column(name = "updated_data", columnDefinition = "NVARCHAR(MAX)")
    private String updatedData;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "change_reason")
    private String changeReason;

    public enum ActionType {
        CREATED, UPDATED, EDITED
    }

    public BookingHistory() {}

    public BookingHistory(String bookingId, Integer versionNumber, ActionType actionType, 
                         String previousData, String updatedData, String changedBy, String changeReason) {
        this.bookingId = bookingId;
        this.versionNumber = versionNumber;
        this.actionType = actionType;
        this.previousData = previousData;
        this.updatedData = updatedData;
        this.changedBy = changedBy;
        this.changeReason = changeReason;
        this.changedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getPreviousData() {
        return previousData;
    }

    public void setPreviousData(String previousData) {
        this.previousData = previousData;
    }

    public String getUpdatedData() {
        return updatedData;
    }

    public void setUpdatedData(String updatedData) {
        this.updatedData = updatedData;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
}
