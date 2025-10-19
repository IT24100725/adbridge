package com.example.adbridge.repo;

import com.example.adbridge.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by payment ID
     */
    Optional<Payment> findByPaymentId(String paymentId);
    
    /**
     * Find payment by booking ID
     */
    Optional<Payment> findByBookingId(String bookingId);
    
    /**
     * Find all payments by booking ID (in case there are multiple payments for same booking)
     */
    List<Payment> findAllByBookingId(String bookingId);
    
    /**
     * Find payments by payment method
     */
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    /**
     * Find payments by payment status
     */
    List<Payment> findByPaymentStatus(Payment.PaymentStatus paymentStatus);
    
    /**
     * Find payments by booking ID and payment method
     */
    Optional<Payment> findByBookingIdAndPaymentMethod(String bookingId, Payment.PaymentMethod paymentMethod);
    
    /**
     * Check if payment exists for booking
     */
    boolean existsByBookingId(String bookingId);
    
    /**
     * Count payments by booking ID
     */
    long countByBookingId(String bookingId);
    
    /**
     * Check if payment exists by payment ID
     */
    boolean existsByPaymentId(String paymentId);
    
    /**
     * Find all non-deleted payments
     */
    List<Payment> findByDeletedFalseOrDeletedIsNull();
    
    /**
     * Find payments by status excluding deleted
     */
    List<Payment> findByPaymentStatusAndDeletedFalseOrDeletedIsNull(Payment.PaymentStatus paymentStatus);
    
    /**
     * Find payments by method excluding deleted
     */
    List<Payment> findByPaymentMethodAndDeletedFalseOrDeletedIsNull(Payment.PaymentMethod paymentMethod);
    
    /**
     * Search payments by payment ID, booking ID or client info
     */
    @Query("SELECT p FROM Payment p JOIN Booking b ON p.bookingId = b.bookingId " +
           "WHERE (p.deleted = false OR p.deleted IS NULL) " +
           "AND (LOWER(p.paymentId) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.bookingId) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Payment> searchPayments(@Param("search") String search);
    
    /**
     * Find payments within date range excluding deleted
     */
    @Query("SELECT p FROM Payment p WHERE (p.deleted = false OR p.deleted IS NULL) " +
           "AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByCreatedAtBetweenAndNotDeleted(@Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find payments by status and date range excluding deleted
     */
    @Query("SELECT p FROM Payment p WHERE (p.deleted = false OR p.deleted IS NULL) " +
           "AND p.paymentStatus = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByStatusAndDateRangeAndNotDeleted(@Param("status") Payment.PaymentStatus status,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find payments by user email (through booking)
     */
    @Query("SELECT p FROM Payment p JOIN Booking b ON p.bookingId = b.bookingId " +
           "WHERE (p.deleted = false OR p.deleted IS NULL) " +
           "AND b.email = :email " +
           "ORDER BY p.createdAt DESC")
    List<Payment> findByUserEmail(@Param("email") String email);
}
