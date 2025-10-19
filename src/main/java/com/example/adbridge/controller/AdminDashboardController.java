package com.example.adbridge.controller;

import com.example.adbridge.repo.*;
import com.example.adbridge.model.SupportRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminDashboardController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SupportRequestRepository supportRepository;

    public AdminDashboardController(BookingRepository bookingRepository, 
                                  PaymentRepository paymentRepository,
                                  TaskRepository taskRepository,
                                  UserRepository userRepository,
                                  SupportRequestRepository supportRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.supportRepository = supportRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        
        // Basic metrics
        long totalUsers = userRepository.count();
        long totalCampaigns = bookingRepository.count();
        long activeCampaigns = bookingRepository.findAll().stream()
            .filter(b -> "APPROVED".equals(b.getPaymentStatus()) && 
                        !"CANCELLED".equals(b.getPaymentStatus()) && 
                        !"CANCELLED".equals(b.getCampaignStatus()))
            .count();
        
        // Financial metrics
        double totalRevenue = paymentRepository.findByPaymentStatusAndDeletedFalseOrDeletedIsNull(
            com.example.adbridge.model.Payment.PaymentStatus.COMPLETED)
            .stream()
            .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
            .sum();
        
        // Task metrics
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.findByStatusOrderByCreatedAtDesc(
            com.example.adbridge.model.Task.TaskStatus.COMPLETED).size();
        long pendingTasks = taskRepository.findByStatusOrderByCreatedAtDesc(
            com.example.adbridge.model.Task.TaskStatus.PENDING).size();
        
        // Support metrics
        long totalSupportTickets = supportRepository.count();
        long openTickets = supportRepository.findByStatusOrderByCreatedAtDesc(SupportRequest.Status.OPEN).size();
        long resolvedTickets = supportRepository.findByStatusOrderByCreatedAtDesc(SupportRequest.Status.RESOLVED).size();
        long closedTickets = supportRepository.findByStatusOrderByCreatedAtDesc(SupportRequest.Status.CLOSED).size();
        
        // Support tickets by priority
        List<SupportRequest> allTickets = supportRepository.findAll();
        Map<SupportRequest.Priority, Long> ticketsByPriority = allTickets.stream()
            .collect(Collectors.groupingBy(SupportRequest::getPriority, Collectors.counting()));
        
        // Recent support activity (last 5 tickets)
        List<SupportRequest> recentTickets = allTickets.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .collect(Collectors.toList());
        
        // Add data to model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCampaigns", totalCampaigns);
        model.addAttribute("activeCampaigns", activeCampaigns);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        
        // Support metrics
        model.addAttribute("totalSupportTickets", totalSupportTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("closedTickets", closedTickets);
        model.addAttribute("ticketsByPriority", ticketsByPriority);
        model.addAttribute("recentTickets", recentTickets);
        
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageHeading", "Dashboard");
        model.addAttribute("title", "Dashboard · Admin");
        
        return "admin/dashboard";
    }
}