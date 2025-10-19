package com.example.adbridge.repo;

import com.example.adbridge.model.CancellationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CancellationActionRepository extends JpaRepository<CancellationAction, Long> {
    List<CancellationAction> findAllByOrderByCreatedAtDesc();
}
