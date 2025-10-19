package com.example.adbridge.service;

import com.example.adbridge.model.StaffMember;
import com.example.adbridge.repo.StaffMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class StaffMemberService {
    
    @Autowired
    private StaffMemberRepository staffMemberRepository;
    
    // Password generation method - simple but not too complicated
    public String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Generate 8-character password
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    // Generate username based on role and name
    public String generateUsername(String fullName, String role) {
        String[] nameParts = fullName.toLowerCase().split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";
        
        // Create base username
        String baseUsername = firstName + (lastName.isEmpty() ? "" : lastName.charAt(0));
        
        // Add role prefix
        String rolePrefix = getRolePrefix(role);
        String username = rolePrefix + baseUsername;
        
        // Check if username exists and add number if needed
        int counter = 1;
        String finalUsername = username;
        while (staffMemberRepository.findByUsername(finalUsername).isPresent()) {
            finalUsername = username + counter;
            counter++;
        }
        
        return finalUsername;
    }
    
    private String getRolePrefix(String role) {
        switch (role.toUpperCase()) {
            case "MANAGING_DIRECTOR":
                return "md_";
            case "MARKETING_PLANNER":
                return "mp_";
            case "ADMIN_ASSISTANT":
                return "aa_";
            case "FINANCE_COORDINATOR":
                return "fc_";
            case "CLIENT_SUPPORT_OFFICER":
                return "cso_";
            case "SENIOR_WEB_DEVELOPER":
                return "swd_";
            default:
                return "staff_";
        }
    }
    
    // Get all staff members
    public List<StaffMember> getAllStaffMembers() {
        return staffMemberRepository.findAllActive();
    }
    
    // Get staff member by ID
    public Optional<StaffMember> getStaffMemberById(Long id) {
        return staffMemberRepository.findById(id);
    }
    
    // Get staff member by username and role (for login)
    public Optional<StaffMember> getStaffMemberByUsernameAndRole(String username, String role) {
        return staffMemberRepository.findByUsernameAndRoleAndIsActiveTrue(username, role);
    }
    
    // Get staff members by role
    public List<StaffMember> getStaffMembersByRole(String role) {
        return staffMemberRepository.findByRoleAndNotDeleted(role);
    }
    
    // Create new staff member
    public StaffMember createStaffMember(StaffMember staffMember, String createdBy) {
        // Generate username and password if not provided
        if (staffMember.getUsername() == null || staffMember.getUsername().isEmpty()) {
            staffMember.setUsername(generateUsername(staffMember.getFullName(), staffMember.getRole()));
        }
        
        if (staffMember.getPassword() == null || staffMember.getPassword().isEmpty()) {
            staffMember.setPassword(generatePassword());
        }
        
        staffMember.setCreatedBy(createdBy);
        staffMember.setIsActive(true);
        staffMember.setIsDeleted(false);
        
        return staffMemberRepository.save(staffMember);
    }
    
    // Update staff member
    public StaffMember updateStaffMember(StaffMember staffMember, String updatedBy) {
        staffMember.setUpdatedBy(updatedBy);
        return staffMemberRepository.save(staffMember);
    }
    
    // Hard delete staff member
    public void deleteStaffMember(Long id, String deletedBy) {
        Optional<StaffMember> staffMemberOpt = staffMemberRepository.findById(id);
        if (staffMemberOpt.isPresent()) {
            // Perform actual database deletion instead of soft delete
            staffMemberRepository.deleteById(id);
        }
    }
    
    // Check if username exists (for validation)
    public boolean usernameExists(String username, Long excludeId) {
        if (excludeId != null) {
            return staffMemberRepository.existsByUsernameAndIdNot(username, excludeId);
        } else {
            return staffMemberRepository.findByUsername(username).isPresent();
        }
    }
    
    // Check if email exists (for validation)
    public boolean emailExists(String email, Long excludeId) {
        if (excludeId != null) {
            return staffMemberRepository.existsByEmailAndIdNot(email, excludeId);
        } else {
            return staffMemberRepository.findByEmail(email).isPresent();
        }
    }
    
    // Get role display names
    public String getRoleDisplayName(String role) {
        switch (role.toUpperCase()) {
            case "MANAGING_DIRECTOR":
                return "Managing Director";
            case "MARKETING_PLANNER":
                return "Marketing Planner";
            case "ADMIN_ASSISTANT":
                return "Admin Assistant";
            case "FINANCE_COORDINATOR":
                return "Finance Coordinator";
            case "CLIENT_SUPPORT_OFFICER":
                return "Client Support Officer";
            case "SENIOR_WEB_DEVELOPER":
                return "Senior Web Developer";
            default:
                return role;
        }
    }
    
    // Get all available roles
    public List<String> getAllRoles() {
        return List.of(
            "MANAGING_DIRECTOR",
            "MARKETING_PLANNER", 
            "ADMIN_ASSISTANT",
            "FINANCE_COORDINATOR",
            "CLIENT_SUPPORT_OFFICER",
            "SENIOR_WEB_DEVELOPER"
        );
    }
}

