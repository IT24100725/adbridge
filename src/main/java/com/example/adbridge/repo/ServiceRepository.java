package com.example.adbridge.repo;

import com.example.adbridge.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    Optional<Service> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
}



