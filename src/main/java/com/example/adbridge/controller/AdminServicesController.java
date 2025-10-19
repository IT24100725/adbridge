package com.example.adbridge.controller;

import com.example.adbridge.model.Service;
import com.example.adbridge.model.AdminRole;
import com.example.adbridge.repo.ServiceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/admin/services")
public class AdminServicesController {

    private final ServiceRepository serviceRepository;

    public AdminServicesController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public String list(Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view services.");
            return "redirect:/admin/dashboard";
        }
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("title", "Services · Admin");
        model.addAttribute("pageHeading", "Services Management");
        model.addAttribute("activeMenu", "services");
        model.addAttribute("services", services);
        return "admin/services/list";
    }

    @GetMapping("/new")
    public String newService(Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view services.");
            return "redirect:/admin/dashboard";
        }
        Service service = new Service();
        model.addAttribute("title", "New Service · Admin");
        model.addAttribute("pageHeading", "Create New Service");
        model.addAttribute("activeMenu", "services");
        model.addAttribute("serviceItem", service);
        model.addAttribute("statuses", Service.Status.values());
        return "admin/services/form";
    }

    @PostMapping
    public String create(@ModelAttribute Service service, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied. You don't have permission to manage services.");
            return "redirect:/admin/dashboard";
        }
        try {
            // Generate slug if not provided
            if (service.getSlug() == null || service.getSlug().trim().isEmpty()) {
                service.setSlug(generateSlug(service.getTitle()));
            }
            
            serviceRepository.save(service);
            redirectAttributes.addFlashAttribute("flashMsg", "Service created successfully!");
            return "redirect:/admin/services";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error creating service: " + e.getMessage());
            return "redirect:/admin/services/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view services.");
            return "redirect:/admin/dashboard";
        }
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
        
        model.addAttribute("title", "Edit Service · Admin");
        model.addAttribute("pageHeading", "Edit Service");
        model.addAttribute("activeMenu", "services");
        model.addAttribute("serviceItem", service);
        model.addAttribute("statuses", Service.Status.values());
        return "admin/services/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Service service, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied. You don't have permission to manage services.");
            return "redirect:/admin/dashboard";
        }
        try {
            Service existingService = serviceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
            
            existingService.setTitle(service.getTitle());
            existingService.setSlug(service.getSlug());
            existingService.setShortDescription(service.getShortDescription());
            existingService.setDescription(service.getDescription());
            existingService.setPrice(service.getPrice());
            existingService.setStatus(service.getStatus());
            
            serviceRepository.save(existingService);
            redirectAttributes.addFlashAttribute("flashMsg", "Service updated successfully!");
            return "redirect:/admin/services";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error updating service: " + e.getMessage());
            return "redirect:/admin/services/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied. You don't have permission to manage services.");
            return "redirect:/admin/dashboard";
        }
        try {
            serviceRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("flashMsg", "Service deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error deleting service: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied. You don't have permission to manage services.");
            return "redirect:/admin/dashboard";
        }
        try {
            Service service = serviceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
            service.setStatus(Service.Status.APPROVED);
            serviceRepository.save(service);
            redirectAttributes.addFlashAttribute("flashMsg", "Service approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error approving service: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user has permission to access services (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied. You don't have permission to manage services.");
            return "redirect:/admin/dashboard";
        }
        try {
            Service service = serviceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
            service.setStatus(Service.Status.REJECTED);
            serviceRepository.save(service);
            redirectAttributes.addFlashAttribute("flashMsg", "Service rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error rejecting service: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    private String generateSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }
}


