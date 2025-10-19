package com.example.adbridge.repo;

import com.example.adbridge.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    // Find contacts by email
    List<Contact> findByEmailOrderByCreatedAtDesc(String email);
    
    // Find unread contacts
    List<Contact> findByIsReadFalseOrderByCreatedAtDesc();
    
    // Find unreplied contacts
    List<Contact> findByIsRepliedFalseOrderByCreatedAtDesc();
    
    // Find contacts created after a specific date
    List<Contact> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
    
    // Count unread contacts
    long countByIsReadFalse();
    
    // Find contacts by subject containing keyword
    @Query("SELECT c FROM Contact c WHERE c.subject LIKE %:keyword% OR c.message LIKE %:keyword% ORDER BY c.createdAt DESC")
    List<Contact> findBySubjectOrMessageContaining(String keyword);
    
    // Find recent contacts (last 30 days)
    @Query("SELECT c FROM Contact c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Contact> findRecentContacts(LocalDateTime since);
}

