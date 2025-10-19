package com.example.adbridge.repo;

import com.example.adbridge.model.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    
    // Find by username
    Optional<StaffMember> findByUsername(String username);
    
    // Find by role
    List<StaffMember> findByRole(String role);
    
    // Find active staff members
    List<StaffMember> findByIsActiveTrue();
    
    // Find active staff members by role
    List<StaffMember> findByRoleAndIsActiveTrue(String role);
    
    // Find by email
    Optional<StaffMember> findByEmail(String email);
    
    // Find by username and role (for login validation)
    Optional<StaffMember> findByUsernameAndRoleAndIsActiveTrue(String username, String role);
    
    // Find all non-deleted staff members
    @Query("SELECT s FROM StaffMember s WHERE s.isDeleted = false")
    List<StaffMember> findAllActive();
    
    // Find by role and not deleted
    @Query("SELECT s FROM StaffMember s WHERE s.role = :role AND s.isDeleted = false")
    List<StaffMember> findByRoleAndNotDeleted(@Param("role") String role);
    
    // Check if username exists (excluding current staff member for updates)
    @Query("SELECT COUNT(s) > 0 FROM StaffMember s WHERE s.username = :username AND s.id != :id AND s.isDeleted = false")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);
    
    // Check if email exists (excluding current staff member for updates)
    @Query("SELECT COUNT(s) > 0 FROM StaffMember s WHERE s.email = :email AND s.id != :id AND s.isDeleted = false")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}

