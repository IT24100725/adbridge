package com.example.adbridge.service;

import com.example.adbridge.model.Booking;
import com.example.adbridge.model.BookingHistory;
import com.example.adbridge.repo.BookingHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VersionHistoryService {

    private final BookingHistoryRepository bookingHistoryRepository;
    private final ObjectMapper objectMapper;

    public VersionHistoryService(BookingHistoryRepository bookingHistoryRepository, ObjectMapper objectMapper) {
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create initial history entry when a booking is first created
     */
    public void createInitialHistory(Booking booking, String createdBy) {
        try {
            String bookingDataJson = objectMapper.writeValueAsString(booking);
            
            BookingHistory history = new BookingHistory(
                booking.getBookingId(),
                1,
                BookingHistory.ActionType.CREATED,
                null, // No previous data for initial creation
                bookingDataJson,
                createdBy,
                "Initial booking creation"
            );
            
            bookingHistoryRepository.save(history);
            
            // Set initial version tracking
            booking.setCurrentVersion(1);
            booking.setLastModifiedAt(LocalDateTime.now());
            booking.setIsEdited(false);
            
        } catch (Exception e) {
            System.err.println("Error creating initial history: " + e.getMessage());
        }
    }

    /**
     * Create history entry when a booking is updated/edited
     */
    public void createUpdateHistory(Booking previousBooking, Booking updatedBooking, String updatedBy, String reason) {
        try {
            // Get the next version number
            Integer nextVersion = getNextVersionNumber(updatedBooking.getBookingId());
            
            // Convert previous and updated data to JSON
            String previousDataJson = objectMapper.writeValueAsString(previousBooking);
            String updatedDataJson = objectMapper.writeValueAsString(updatedBooking);
            
            BookingHistory history = new BookingHistory(
                updatedBooking.getBookingId(),
                nextVersion,
                BookingHistory.ActionType.EDITED,
                previousDataJson,
                updatedDataJson,
                updatedBy,
                reason != null ? reason : "Booking updated"
            );
            
            bookingHistoryRepository.save(history);
            
            // Update version tracking
            updatedBooking.setCurrentVersion(nextVersion);
            updatedBooking.setLastModifiedAt(LocalDateTime.now());
            updatedBooking.setIsEdited(true);
            
        } catch (Exception e) {
            System.err.println("Error creating update history: " + e.getMessage());
        }
    }

    /**
     * Get all history entries for a booking
     */
    public List<BookingHistory> getBookingHistory(String bookingId) {
        return bookingHistoryRepository.findByBookingIdOrderByVersionNumberDesc(bookingId);
    }

    /**
     * Get the latest version number for a booking
     */
    public Integer getNextVersionNumber(String bookingId) {
        Integer latestVersion = bookingHistoryRepository.findLatestVersionNumber(bookingId);
        return latestVersion != null ? latestVersion + 1 : 1;
    }

    /**
     * Get a specific version of a booking from history
     */
    public BookingHistory getBookingVersion(String bookingId, Integer versionNumber) {
        List<BookingHistory> history = bookingHistoryRepository.findByBookingIdOrderByVersionNumberDesc(bookingId);
        return history.stream()
                .filter(h -> h.getVersionNumber().equals(versionNumber))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if a booking has been edited
     */
    public boolean hasBookingBeenEdited(String bookingId) {
        List<BookingHistory> history = bookingHistoryRepository.findByBookingIdAndActionTypeOrderByVersionNumberDesc(
            bookingId, BookingHistory.ActionType.EDITED);
        return !history.isEmpty();
    }

    /**
     * Get the previous version of a booking for comparison
     */
    public BookingHistory getPreviousVersion(String bookingId) {
        List<BookingHistory> history = bookingHistoryRepository.findByBookingIdOrderByVersionNumberDesc(bookingId);
        return history.size() > 1 ? history.get(1) : null; // Skip current version, get previous
    }
}
