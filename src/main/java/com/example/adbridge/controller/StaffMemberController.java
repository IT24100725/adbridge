package com.example.adbridge.controller;

import com.example.adbridge.model.AdminRole;
import com.example.adbridge.model.StaffMember;
import com.example.adbridge.service.StaffMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/staff")
public class StaffMemberController {
    
    @Autowired
    private StaffMemberService staffMemberService;
    
    @GetMapping
    public String staffList(Model model, HttpSession session, RedirectAttributes ra) {
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }
        
        // Get current user's role
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        String currentRoleString = getRoleString(currentRole);
        
        // Get all staff members
        List<StaffMember> staffMembers = staffMemberService.getAllStaffMembers();
        
        model.addAttribute("staffMembers", staffMembers);
        model.addAttribute("currentRole", currentRoleString);
        model.addAttribute("currentRoleEnum", currentRole);
        model.addAttribute("roles", staffMemberService.getAllRoles());
        model.addAttribute("pageHeading", "Staff Management");
        
        return "admin/settings/staff-list";
    }
    
    @PostMapping("/add")
    public String addStaffMember(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam String position,
            @RequestParam String role,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            HttpSession session,
            RedirectAttributes ra) {
        
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }
        
        // Only senior web developer can add staff members
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole != AdminRole.DEVELOPER) {
            ra.addFlashAttribute("error", "Access denied. Only Senior Web Developer can add staff members.");
            return "redirect:/admin/staff";
        }
        
        try {
            // Validate email uniqueness
            if (staffMemberService.emailExists(email, null)) {
                ra.addFlashAttribute("error", "Email already exists. Please use a different email.");
                return "redirect:/admin/staff";
            }
            
            // Validate username uniqueness if provided
            if (username != null && !username.isEmpty() && staffMemberService.usernameExists(username, null)) {
                ra.addFlashAttribute("error", "Username already exists. Please use a different username.");
                return "redirect:/admin/staff";
            }
            
            // Create staff member
            StaffMember staffMember = new StaffMember();
            staffMember.setFullName(fullName);
            staffMember.setEmail(email);
            staffMember.setPhone(phone);
            staffMember.setAddress(address);
            staffMember.setPosition(position);
            staffMember.setRole(role);
            
            // Set username and password if provided, otherwise generate
            if (username != null && !username.isEmpty()) {
                staffMember.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                staffMember.setPassword(password);
            }
            
            staffMemberService.createStaffMember(staffMember, "admin");
            ra.addFlashAttribute("success", "Staff member added successfully!");
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error adding staff member: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    @PostMapping("/{id}/update")
    public String updateStaffMember(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam String position,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            HttpSession session,
            RedirectAttributes ra) {
        
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }
        
        // Only senior web developer can update staff members
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole != AdminRole.DEVELOPER) {
            ra.addFlashAttribute("error", "Access denied. Only Senior Web Developer can update staff members.");
            return "redirect:/admin/staff";
        }
        
        try {
            Optional<StaffMember> staffMemberOpt = staffMemberService.getStaffMemberById(id);
            if (staffMemberOpt.isEmpty()) {
                ra.addFlashAttribute("error", "Staff member not found.");
                return "redirect:/admin/staff";
            }
            
            StaffMember staffMember = staffMemberOpt.get();
            
            // Validate email uniqueness (excluding current staff member)
            if (staffMemberService.emailExists(email, id)) {
                ra.addFlashAttribute("error", "Email already exists. Please use a different email.");
                return "redirect:/admin/staff";
            }
            
            // Validate username uniqueness if provided (excluding current staff member)
            if (username != null && !username.isEmpty() && staffMemberService.usernameExists(username, id)) {
                ra.addFlashAttribute("error", "Username already exists. Please use a different username.");
                return "redirect:/admin/staff";
            }
            
            // Update staff member
            staffMember.setFullName(fullName);
            staffMember.setEmail(email);
            staffMember.setPhone(phone);
            staffMember.setAddress(address);
            staffMember.setPosition(position);
            
            if (username != null && !username.isEmpty()) {
                staffMember.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                staffMember.setPassword(password);
            }
            
            staffMemberService.updateStaffMember(staffMember, "admin");
            ra.addFlashAttribute("success", "Staff member updated successfully!");
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating staff member: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteStaffMember(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }
        
        // Only senior web developer can delete staff members
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole != AdminRole.DEVELOPER) {
            ra.addFlashAttribute("error", "Access denied. Only Senior Web Developer can delete staff members.");
            return "redirect:/admin/staff";
        }
        
        try {
            staffMemberService.deleteStaffMember(id, "admin");
            ra.addFlashAttribute("success", "Staff member deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting staff member: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    @PostMapping("/{id}/generate-credentials")
    public String generateCredentials(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }
        
        // Only senior web developer can generate credentials
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole != AdminRole.DEVELOPER) {
            ra.addFlashAttribute("error", "Access denied. Only Senior Web Developer can generate credentials.");
            return "redirect:/admin/staff";
        }
        
        try {
            Optional<StaffMember> staffMemberOpt = staffMemberService.getStaffMemberById(id);
            if (staffMemberOpt.isEmpty()) {
                ra.addFlashAttribute("error", "Staff member not found.");
                return "redirect:/admin/staff";
            }
            
            StaffMember staffMember = staffMemberOpt.get();
            
            // Generate new username and password
            String newUsername = staffMemberService.generateUsername(staffMember.getFullName(), staffMember.getRole());
            String newPassword = staffMemberService.generatePassword();
            
            staffMember.setUsername(newUsername);
            staffMember.setPassword(newPassword);
            
            staffMemberService.updateStaffMember(staffMember, "admin");
            ra.addFlashAttribute("success", "New credentials generated successfully! Username: " + newUsername + ", Password: " + newPassword);
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error generating credentials: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    private String getRoleString(AdminRole role) {
        if (role == null) return "";
        
        switch (role) {
            case SUPER_ADMIN:
                return "SUPER_ADMIN";
            case ADMIN:
                return "ADMIN_ASSISTANT";
            case FINANCE:
                return "FINANCE_COORDINATOR";
            case MARKETING:
                return "MARKETING_PLANNER";
            case SUPPORT:
                return "CLIENT_SUPPORT_OFFICER";
            case DEVELOPER:
                return "SENIOR_WEB_DEVELOPER";
            case DIRECTOR:
                return "MANAGING_DIRECTOR";
            default:
                return "";
        }
    }
}

