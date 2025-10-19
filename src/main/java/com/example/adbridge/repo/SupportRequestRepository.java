package com.example.adbridge.repo;

import com.example.adbridge.model.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    List<SupportRequest> findByStatusOrderByCreatedAtDesc(SupportRequest.Status status);
    List<SupportRequest> findByAssignedToOrderByCreatedAtDesc(String assignedTo);
}





