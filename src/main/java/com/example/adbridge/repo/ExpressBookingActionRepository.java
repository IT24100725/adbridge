package com.example.adbridge.repo;

import com.example.adbridge.model.ExpressBookingAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpressBookingActionRepository extends JpaRepository<ExpressBookingAction, Long> {
    
    List<ExpressBookingAction> findAllByOrderByCreatedAtDesc();
    
    List<ExpressBookingAction> findByActionOrderByCreatedAtDesc(String action);
    
    List<ExpressBookingAction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}

