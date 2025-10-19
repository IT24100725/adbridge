package com.example.adbridge.controller;

import com.example.adbridge.model.AdminRole;
import com.example.adbridge.model.StaffMember;
import com.example.adbridge.service.StaffMemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminAuthController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    public static final String SESSION_KEY = "ADMIN_LOGGED_IN";
    
    @Autowired
    private StaffMemberService staffMemberService;

    @GetMapping("/admin/login")
    public String adminLoginPage(HttpSession session, Model model) {
        if (Boolean.TRUE.equals(session.getAttribute(SESSION_KEY))) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("loginPage", true);
        model.addAttribute("title", "Admin Login");
        return "admin/admin-login";
    }

    @PostMapping("/admin/login")
    public String doLogin(@RequestParam(name = "username") String username,
                          @RequestParam(name = "password") String password,
                          @RequestParam(name = "role") String role,
                          HttpServletRequest request,
                          RedirectAttributes ra) {

        // First check regular admin credentials
        if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_KEY, true);
            session.setAttribute("ADMIN_ROLE", AdminRole.valueOf(role));
            log.debug("Admin login success. Session id={}, Role={}", session.getId(), role);
            return "redirect:/admin/dashboard";
        }
        
        // If not regular admin, check staff members table
        try {
            // Convert login form role to database role format
            String dbRole = convertLoginRoleToDbRole(role);
            log.debug("Checking staff member login: username={}, role={}, dbRole={}", username, role, dbRole);
            
            Optional<StaffMember> staffMember = staffMemberService.getStaffMemberByUsernameAndRole(username, dbRole);
            if (staffMember.isPresent()) {
                log.debug("Found staff member: {}, password match: {}", staffMember.get().getFullName(), 
                         staffMember.get().getPassword().equals(password));
                if (staffMember.get().getPassword().equals(password)) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute(SESSION_KEY, true);
                    session.setAttribute("ADMIN_ROLE", AdminRole.valueOf(role));
                    session.setAttribute("STAFF_MEMBER_ID", staffMember.get().getId());
                    log.debug("Staff member login success. Session id={}, Role={}, Staff ID={}", 
                             session.getId(), role, staffMember.get().getId());
                    return "redirect:/admin/dashboard";
                }
            } else {
                log.debug("No staff member found with username={} and role={}", username, dbRole);
            }
        } catch (Exception e) {
            log.error("Error checking staff member credentials", e);
        }
        
        ra.addFlashAttribute("loginError", "Invalid credentials");
        return "redirect:/admin/login?error";
    }

    @GetMapping("/admin/logout")
    public String logoutGet(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.debug("Admin logout. Session id={}", session.getId());
            session.invalidate();
        }
        return "redirect:/";
    }

    @PostMapping("/admin/logout")
    public String logoutPost(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.debug("Admin logout (POST). Session id={}", session.getId());
            session.invalidate();
        }
        return "redirect:/";
    }
    
    /**
     * Convert login form role to database role format
     */
    private String convertLoginRoleToDbRole(String loginRole) {
        switch (loginRole) {
            case "ADMIN":
                return "ADMIN_ASSISTANT";
            case "FINANCE":
                return "FINANCE_COORDINATOR";
            case "MARKETING":
                return "MARKETING_PLANNER";
            case "SUPPORT":
                return "CLIENT_SUPPORT_OFFICER";
            case "DEVELOPER":
                return "SENIOR_WEB_DEVELOPER";
            case "DIRECTOR":
                return "MANAGING_DIRECTOR";
            default:
                return loginRole; // Return as-is if no conversion needed
        }
    }
}