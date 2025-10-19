package com.example.adbridge.repo;

import com.example.adbridge.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingId(String bookingId);
    List<Booking> findByEmailOrderByIdDesc(String email);
}

















