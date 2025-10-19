package com.example.adbridge.repo;

import com.example.adbridge.model.CommunicationHistory;
import com.example.adbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunicationHistoryRepository extends JpaRepository<CommunicationHistory, Long> {
    List<CommunicationHistory> findByUserOrderByCreatedAtDesc(User user);
    List<CommunicationHistory> findByUserAndTypeOrderByCreatedAtDesc(User user, CommunicationHistory.CommunicationType type);
}


