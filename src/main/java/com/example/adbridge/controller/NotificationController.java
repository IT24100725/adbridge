package com.example.adbridge.controller;

import com.example.adbridge.model.Notification;
import com.example.adbridge.model.User;
import com.example.adbridge.repo.NotificationRepository;
import com.example.adbridge.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public String listNotifications(Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Get all notifications for this user, ordered by newest first
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        
        // Group notifications by request type based on title content
        List<Notification> createClientNotifications = notifications.stream()
                .filter(n -> n.getTitle().toLowerCase().contains("new client") || 
                           n.getTitle().toLowerCase().contains("client request") ||
                           n.getTitle().toLowerCase().contains("client approved"))
                .collect(Collectors.toList());
        
        List<Notification> cancelBookingNotifications = notifications.stream()
                .filter(n -> n.getTitle().toLowerCase().contains("cancellation") || 
                           n.getTitle().toLowerCase().contains("cancel booking") ||
                           n.getTitle().toLowerCase().contains("booking cancel"))
                .collect(Collectors.toList());
        
        List<Notification> rejectedBookingNotifications = notifications.stream()
                .filter(n -> n.getTitle().toLowerCase().contains("re-approval") || 
                           n.getTitle().toLowerCase().contains("rejected booking") ||
                           n.getTitle().toLowerCase().contains("booking re-approval"))
                .collect(Collectors.toList());
        
        List<Notification> generalInquiryNotifications = notifications.stream()
                .filter(n -> n.getTitle().toLowerCase().contains("general inquiry") || 
                           n.getTitle().toLowerCase().contains("inquiry") ||
                           n.getTitle().toLowerCase().contains("contact message"))
                .collect(Collectors.toList());
        
        // Get counts for each type
        long createClientCount = createClientNotifications.size();
        long cancelBookingCount = cancelBookingNotifications.size();
        long rejectedBookingCount = rejectedBookingNotifications.size();
        long generalInquiryCount = generalInquiryNotifications.size();
        
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);
        model.addAttribute("createClientNotifications", createClientNotifications);
        model.addAttribute("cancelBookingNotifications", cancelBookingNotifications);
        model.addAttribute("rejectedBookingNotifications", rejectedBookingNotifications);
        model.addAttribute("generalInquiryNotifications", generalInquiryNotifications);
        model.addAttribute("createClientCount", createClientCount);
        model.addAttribute("cancelBookingCount", cancelBookingCount);
        model.addAttribute("rejectedBookingCount", rejectedBookingCount);
        model.addAttribute("generalInquiryCount", generalInquiryCount);
        
        return "notifications/list";
    }
    
    @PostMapping("/{notificationId}/mark-read")
    public String markAsRead(@PathVariable Long notificationId, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Get notification and verify it belongs to the user
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getUser().getUserId().equals(user.getUserId())) {
            notification.setIsRead(true);
            notification.setReadAt(java.time.LocalDateTime.now());
            notificationRepository.save(notification);
        }
        
        return "redirect:/notifications";
    }
    
    @PostMapping("/mark-all-read")
    public String markAllAsRead(HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Mark all unread notifications as read
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(java.time.LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);
        
        return "redirect:/notifications";
    }
}
