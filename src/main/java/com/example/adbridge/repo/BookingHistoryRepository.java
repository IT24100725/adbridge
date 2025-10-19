package com.example.adbridge.repo;

import com.example.adbridge.model.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {
    
    // Find all history entries for a specific booking
    List<BookingHistory> findByBookingIdOrderByVersionNumberDesc(String bookingId);
    
    // Find the latest version of a booking
    @Query("SELECT MAX(bh.versionNumber) FROM BookingHistory bh WHERE bh.bookingId = :bookingId")
    Integer findLatestVersionNumber(@Param("bookingId") String bookingId);
    
    // Find history entries by action type
    List<BookingHistory> findByBookingIdAndActionTypeOrderByVersionNumberDesc(
        String bookingId, BookingHistory.ActionType actionType);
    
    // Find all history entries for a booking with pagination
    @Query("SELECT bh FROM BookingHistory bh WHERE bh.bookingId = :bookingId ORDER BY bh.versionNumber DESC")
    List<BookingHistory> findBookingHistoryWithPagination(@Param("bookingId") String bookingId);
}
