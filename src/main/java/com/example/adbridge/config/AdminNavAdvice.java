package com.example.adbridge.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.example.adbridge.controller.AdminAuthController;
import com.example.adbridge.model.AdminRole;

@ControllerAdvice(basePackages = "com.example.adbridge.controller")
public class AdminNavAdvice {

    @ModelAttribute("activePage")
    public String activePage() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "";
        HttpServletRequest req = attrs.getRequest();
        String uri = req.getRequestURI();
        if (uri.startsWith("/admin/services")) return "services";
        if (uri.startsWith("/admin/dashboard")) return "dashboard";
        return "";
    }

    @ModelAttribute("adminRole")
    public AdminRole adminRole() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest req = attrs.getRequest();
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object r = session.getAttribute("ADMIN_ROLE");
        return (r instanceof AdminRole) ? (AdminRole) r : null;
    }
}