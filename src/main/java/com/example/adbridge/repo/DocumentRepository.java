package com.example.adbridge.repo;

import com.example.adbridge.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTitleContainingIgnoreCase(String title);
    List<Document> findByCategory(String category);
    List<Document> findByIsPublicTrue();
    List<Document> findByUploadedByContainingIgnoreCase(String uploadedBy);
    List<Document> findByTitleContainingIgnoreCaseAndUploadedByContainingIgnoreCase(String title, String uploadedBy);
}