package com.example.adbridge.repo;

import com.example.adbridge.model.SupportComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportCommentRepository extends JpaRepository<SupportComment, Long> {
    List<SupportComment> findBySupportRequestIdOrderByCreatedAtAsc(Long supportRequestId);
}





