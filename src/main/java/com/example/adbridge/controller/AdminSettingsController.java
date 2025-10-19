package com.example.adbridge.controller;

import com.example.adbridge.model.SystemSetting;
import com.example.adbridge.model.AdminRole;
import com.example.adbridge.repo.SystemSettingRepository;
import com.example.adbridge.controller.AdminAuthController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @GetMapping
    public String settingsList(Model model, HttpSession session, RedirectAttributes ra) {
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }

        // Check role-based access (SUPER_ADMIN, DEVELOPER only)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPER_ADMIN && role != AdminRole.DEVELOPER) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to access settings.");
            return "redirect:/admin/dashboard";
        }

        List<SystemSetting> settings = systemSettingRepository.findAll();
        model.addAttribute("settings", settings);
        model.addAttribute("pageHeading", "Settings");
        
        return "admin/settings/list";
    }

    @PostMapping
    public String createOrUpdateSetting(
            @RequestParam String key,
            @RequestParam(required = false) String value,
            @RequestParam(required = false) String description,
            HttpSession session,
            RedirectAttributes ra) {
        
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }

        // Check role-based access (SUPER_ADMIN, DEVELOPER only)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPER_ADMIN && role != AdminRole.DEVELOPER) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to access settings.");
            return "redirect:/admin/dashboard";
        }

        try {
            // Check if setting already exists
            var existingSetting = systemSettingRepository.findByKey(key);
            
            if (existingSetting.isPresent()) {
                // Update existing setting
                SystemSetting setting = existingSetting.get();
                setting.setValue(value);
                setting.setDescription(description);
                setting.setUpdatedBy("admin");
                systemSettingRepository.save(setting);
                ra.addFlashAttribute("success", "Setting updated successfully");
            } else {
                // Create new setting
                SystemSetting newSetting = new SystemSetting(key, value, description);
                newSetting.setUpdatedBy("admin");
                systemSettingRepository.save(newSetting);
                ra.addFlashAttribute("success", "Setting created successfully");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving setting: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    @PostMapping("/{id}/delete")
    public String deleteSetting(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check admin authentication
        if (!Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY))) {
            return "redirect:/admin/login";
        }

        // Check role-based access (SUPER_ADMIN, DEVELOPER only)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPER_ADMIN && role != AdminRole.DEVELOPER) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to access settings.");
            return "redirect:/admin/dashboard";
        }

        try {
            systemSettingRepository.deleteById(id);
            ra.addFlashAttribute("success", "Setting deleted successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting setting: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }
}
