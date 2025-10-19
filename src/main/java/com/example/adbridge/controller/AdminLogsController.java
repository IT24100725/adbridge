package com.example.adbridge.controller;

import com.example.adbridge.model.AdminRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/logs")
public class AdminLogsController {

    private final Path logPath = Paths.get("logs", "application.log");

    @GetMapping
    public String viewLogs(Model model, HttpSession session, RedirectAttributes ra) throws IOException {
        // Check if user has permission to access logs (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view logs.");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("title", "System Logs · Admin");
        model.addAttribute("pageHeading", "System Logs");
        model.addAttribute("activeMenu", "logs");

        List<String> lines;
        if (Files.exists(logPath)) {
            lines = Files.readAllLines(logPath);
            // show last 300 lines max
            if (lines.size() > 300) {
                lines = lines.subList(lines.size() - 300, lines.size());
            }
        } else {
            lines = List.of("No log file found at " + logPath.toString());
        }
        model.addAttribute("logLines", lines);
        return "admin/logs/index";
    }
}





