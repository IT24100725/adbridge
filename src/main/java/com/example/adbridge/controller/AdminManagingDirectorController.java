package com.example.adbridge.controller;

import com.example.adbridge.model.AdminRole;
import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Payment;
import com.example.adbridge.model.User;
import com.example.adbridge.model.Notification;
import com.example.adbridge.model.SupportRequest;
import com.example.adbridge.model.ExpressBookingAction;
import com.example.adbridge.model.CancellationAction;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.PaymentRepository;
import com.example.adbridge.repo.UserRepository;
import com.example.adbridge.repo.NotificationRepository;
import com.example.adbridge.repo.SupportRequestRepository;
import com.example.adbridge.repo.ExpressBookingActionRepository;
import com.example.adbridge.repo.CancellationActionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminManagingDirectorController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ExpressBookingActionRepository expressBookingActionRepository;
    private final CancellationActionRepository cancellationActionRepository;

    public AdminManagingDirectorController(BookingRepository bookingRepository, 
                                          PaymentRepository paymentRepository,
                                          SupportRequestRepository supportRequestRepository,
                                          UserRepository userRepository,
                                          NotificationRepository notificationRepository,
                                          ExpressBookingActionRepository expressBookingActionRepository,
                                          CancellationActionRepository cancellationActionRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.expressBookingActionRepository = expressBookingActionRepository;
        this.cancellationActionRepository = cancellationActionRepository;
    }

    @GetMapping("/admin/managing-director/campaigns")
    public String campaignsList(@RequestParam(value = "status", required = false, defaultValue = "PENDING") String status, Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access managing director campaigns (only Managing Director and Super Admin)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Managing Director can access this page.");
            return "redirect:/admin/dashboard";
        }

        System.out.println("=== MANAGING DIRECTOR CAMPAIGNS PAGE ACCESSED ===");

        model.addAttribute("title", "Campaigns Approval · Managing Director");
        model.addAttribute("pageHeading", "Campaigns Approval");
        model.addAttribute("activeMenu", "managing-director");

        // Get all marketing campaigns (created by Marketing Planner)
        var allMarketingCampaigns = bookingRepository.findAll().stream()
                .filter(b -> b.getCurrentVersion() != null)
                .filter(b -> !"CANCELLED".equals(b.getPaymentStatus()) && !"CANCELLED".equals(b.getCampaignStatus()))
                .toList();

        // Filter campaigns based on status parameter
        var filteredCampaigns = allMarketingCampaigns.stream()
                .filter(b -> {
                    switch (status.toUpperCase()) {
                        case "PENDING":
                            return "PENDING".equals(b.getPaymentStatus()) && "PENDING".equals(b.getCampaignStatus());
                        case "APPROVED":
                            return "COMPLETED".equals(b.getPaymentStatus()) && "APPROVED".equals(b.getCampaignStatus());
                        case "REJECTED":
                            return "REJECTED".equals(b.getPaymentStatus()) && "REJECTED".equals(b.getCampaignStatus());
                        case "EXPRESS":
                            return "COMPLETED".equals(b.getPaymentStatus()) && "COMPLETED".equals(b.getCampaignStatus());
                        default:
                            return "PENDING".equals(b.getPaymentStatus()) && "PENDING".equals(b.getCampaignStatus());
                    }
                })
                .toList();

        // Calculate counts for each status
        long pendingCount = allMarketingCampaigns.stream()
                .filter(b -> "PENDING".equals(b.getPaymentStatus()) && "PENDING".equals(b.getCampaignStatus()))
                .count();
        long approvedCount = allMarketingCampaigns.stream()
                .filter(b -> "COMPLETED".equals(b.getPaymentStatus()) && "APPROVED".equals(b.getCampaignStatus()))
                .count();
        long rejectedCount = allMarketingCampaigns.stream()
                .filter(b -> "REJECTED".equals(b.getPaymentStatus()) && "REJECTED".equals(b.getCampaignStatus()))
                .count();
        long expressCount = allMarketingCampaigns.stream()
                .filter(b -> "COMPLETED".equals(b.getPaymentStatus()) && "COMPLETED".equals(b.getCampaignStatus()))
                .count();

        model.addAttribute("campaigns", filteredCampaigns);
        model.addAttribute("currentStatus", status);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("expressCount", expressCount);
        return "admin/managing-director/campaigns/list";
    }

    @PostMapping("/admin/managing-director/campaigns/{id}/approve")
    public String approveCampaign(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to approve campaigns (only Managing Director and Super Admin)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Managing Director can approve campaigns.");
            return "redirect:/admin/managing-director/campaigns";
        }

        bookingRepository.findById(id).ifPresent(b -> {
            // Managing Director approves - everything becomes COMPLETED/APPROVED
            b.setPaymentStatus("COMPLETED");
            b.setCampaignStatus("APPROVED");
            bookingRepository.save(b);

            // Also update Payment entity to COMPLETED
            List<Payment> payments = paymentRepository.findAllByBookingId(b.getBookingId());
            payments.stream()
                    .filter(payment -> payment.getDeleted() == null || !payment.getDeleted())
                    .forEach(payment -> {
                        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                        paymentRepository.save(payment);
                    });
        });

        ra.addFlashAttribute("success", "Campaign approved successfully. Payment and campaign are now completed.");
        return "redirect:/admin/managing-director/campaigns";
    }

    @PostMapping("/admin/managing-director/campaigns/{id}/reject")
    public String rejectCampaign(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to reject campaigns (only Managing Director and Super Admin)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Managing Director can reject campaigns.");
            return "redirect:/admin/managing-director/campaigns";
        }

        bookingRepository.findById(id).ifPresent(b -> {
            // Managing Director rejects - everything becomes REJECTED
            b.setPaymentStatus("REJECTED");
            b.setCampaignStatus("REJECTED");
            bookingRepository.save(b);

            // Also update Payment entity to CANCELLED
            List<Payment> payments = paymentRepository.findAllByBookingId(b.getBookingId());
            payments.stream()
                    .filter(payment -> payment.getDeleted() == null || !payment.getDeleted())
                    .forEach(payment -> {
                        payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
                        paymentRepository.save(payment);
                    });
        });

        ra.addFlashAttribute("success", "Campaign rejected successfully. Payment and campaign are now rejected.");
        return "redirect:/admin/managing-director/campaigns";
    }

    @GetMapping("/admin/managing-director/campaigns/{id}")
    public String viewCampaign(@PathVariable Long id, HttpSession session, RedirectAttributes ra, Model model) {
        // Check if user has permission to view campaigns (only Managing Director and Super Admin)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Managing Director can view campaigns.");
            return "redirect:/admin/dashboard";
        }

        // Find the campaign by ID
        Optional<Booking> campaignOpt = bookingRepository.findById(id);
        if (campaignOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Campaign not found.");
            return "redirect:/admin/managing-director/campaigns";
        }

        Booking campaign = campaignOpt.get();
        model.addAttribute("campaign", campaign);
        model.addAttribute("title", "Campaign Details · Admin");
        model.addAttribute("pageHeading", "Campaign Details");
        model.addAttribute("activeMenu", "campaigns");

        return "admin/managing-director/campaigns/detail";
    }

    @PostMapping("/admin/managing-director/campaigns/approve-by-booking/{bookingId}")
    public String approveByBookingId(@PathVariable String bookingId, 
                                     @RequestParam(required = false) Long ticketId,
                                     HttpSession session, RedirectAttributes ra) {
        // Add debug logging
        System.out.println("=== EXPRESS APPROVAL DEBUG ===");
        System.out.println("Received bookingId: [" + bookingId + "]");
        System.out.println("BookingId length: " + bookingId.length());
        
        // Clean the booking ID (remove leading/trailing comma and trim whitespace)
        String cleanBookingId = bookingId.trim();
        if (cleanBookingId.startsWith(",")) {
            cleanBookingId = cleanBookingId.substring(1).trim();
        }
        if (cleanBookingId.endsWith(",")) {
            cleanBookingId = cleanBookingId.substring(0, cleanBookingId.length() - 1).trim();
        }
        System.out.println("Cleaned bookingId: [" + cleanBookingId + "]");
        
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Managing Director can approve bookings.");
            return "redirect:/admin/support";
        }

        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(cleanBookingId);
        System.out.println("Booking found: " + bookingOpt.isPresent());
        
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            System.out.println("Current payment status: " + booking.getPaymentStatus());
            System.out.println("Current campaign status: " + booking.getCampaignStatus());
            
            // Approve payment
            booking.setPaymentStatus("COMPLETED");
            
            // Set to COMPLETED to hide from Marketing (not PENDING/REJECTED/APPROVED)
            booking.setCampaignStatus("COMPLETED");
            
            bookingRepository.save(booking);
            System.out.println("Booking updated successfully");
            
            // Update Payment entity
            List<Payment> payments = paymentRepository.findAllByBookingId(cleanBookingId);
            System.out.println("Found " + payments.size() + " payments");
            payments.stream()
                .filter(payment -> !payment.getDeleted())
                .forEach(payment -> {
                    payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                    paymentRepository.save(payment);
                });
            
            // Send notification to user automatically
            List<User> users = userRepository.findByEmail(booking.getEmail());
            if (!users.isEmpty()) {
                User user = users.get(0); // Get first user with this email
                Notification notification = new Notification(
                    user,
                    "Booking Approved (Express)",
                    "Your booking " + cleanBookingId + " has been approved by the Managing Director. No campaign creation required.",
                    Notification.NotificationType.SUCCESS
                );
                notificationRepository.save(notification);
            }
            
            // Log express action
            ExpressBookingAction action = new ExpressBookingAction(
                cleanBookingId,
                "APPROVED",
                "Managing Director",
                booking.getFullName(),
                booking.getEmail(),
                booking.getTotalAmount().doubleValue(),
                booking.getServiceType().toString()
            );
            expressBookingActionRepository.save(action);
            
            // Update ticket status if ticket ID provided
            if (ticketId != null) {
                Optional<SupportRequest> ticketOpt = supportRequestRepository.findById(ticketId);
                if (ticketOpt.isPresent()) {
                    SupportRequest ticket = ticketOpt.get();
                    ticket.setStatus(SupportRequest.Status.IN_PROGRESS);
                    ticket.setExpressAction("APPROVED");
                    supportRequestRepository.save(ticket);
                }
            }
            
            ra.addFlashAttribute("success", "Booking approved. Click 'Mark as Complete' to forward to Client Support Officer.");
        } else {
            System.out.println("ERROR: Booking not found for ID: [" + cleanBookingId + "]");
            ra.addFlashAttribute("error", "Booking not found: " + cleanBookingId);
        }
        
        return "redirect:/admin/support";
    }

    @PostMapping("/admin/managing-director/campaigns/reject-by-booking/{bookingId}")
    public String rejectByBookingId(@PathVariable String bookingId,
                                    @RequestParam(required = false) Long ticketId,
                                    HttpSession session, RedirectAttributes ra) {
        // Clean booking ID
        String cleanBookingId = bookingId.trim();
        if (cleanBookingId.startsWith(",")) {
            cleanBookingId = cleanBookingId.substring(1).trim();
        }
        if (cleanBookingId.endsWith(",")) {
            cleanBookingId = cleanBookingId.substring(0, cleanBookingId.length() - 1).trim();
        }
        
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied.");
            return "redirect:/admin/support";
        }

        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(cleanBookingId);
        
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Reject payment and campaign
            booking.setPaymentStatus("REJECTED");
            booking.setCampaignStatus("REJECTED");
            bookingRepository.save(booking);
            
            // Update Payment entity
            List<Payment> payments = paymentRepository.findAllByBookingId(cleanBookingId);
            payments.stream()
                .filter(payment -> !payment.getDeleted())
                .forEach(payment -> {
                    payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                });
            
            // Send notification to user automatically
            List<User> users = userRepository.findByEmail(booking.getEmail());
            if (!users.isEmpty()) {
                User user = users.get(0); // Get first user with this email
                Notification notification = new Notification(
                    user,
                    "Booking Rejected (Express)",
                    "Your booking " + cleanBookingId + " has been rejected by the Managing Director.",
                    Notification.NotificationType.ERROR
                );
                notificationRepository.save(notification);
            }
            
            // Log express action
            ExpressBookingAction action = new ExpressBookingAction(
                cleanBookingId,
                "REJECTED",
                "Managing Director",
                booking.getFullName(),
                booking.getEmail(),
                booking.getTotalAmount().doubleValue(),
                booking.getServiceType().toString()
            );
            expressBookingActionRepository.save(action);
            
            // Update ticket status if ticket ID provided
            if (ticketId != null) {
                Optional<SupportRequest> ticketOpt = supportRequestRepository.findById(ticketId);
                if (ticketOpt.isPresent()) {
                    SupportRequest ticket = ticketOpt.get();
                    ticket.setStatus(SupportRequest.Status.IN_PROGRESS);
                    ticket.setExpressAction("REJECTED");
                    supportRequestRepository.save(ticket);
                }
            }
            
            ra.addFlashAttribute("success", "Booking rejected. Click 'Mark as Complete' to forward to Client Support Officer.");
        } else {
            ra.addFlashAttribute("error", "Booking not found: " + cleanBookingId);
        }
        
        return "redirect:/admin/support";
    }

    @PostMapping("/admin/support/mark-complete/{ticketId}")
    public String markTicketComplete(@PathVariable Long ticketId, HttpSession session, RedirectAttributes ra) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied.");
            return "redirect:/admin/support";
        }
        
        Optional<SupportRequest> ticketOpt = supportRequestRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportRequest ticket = ticketOpt.get();
            ticket.setStatus(SupportRequest.Status.RESOLVED);
            ticket.setExpressCompletedAt(java.time.LocalDateTime.now());
            supportRequestRepository.save(ticket);
            
            ra.addFlashAttribute("success", "Ticket forwarded to Client Support Officer.");
        } else {
            ra.addFlashAttribute("error", "Ticket not found.");
        }
        
        return "redirect:/admin/support";
    }

    @PostMapping("/admin/managing-director/campaigns/cancel-approve-by-booking/{bookingId}")
    public String approveCancellation(@PathVariable String bookingId, 
                                     @RequestParam(required = false) Long ticketId,
                                     HttpSession session, RedirectAttributes ra) {
        String cleanBookingId = bookingId.trim();
        if (cleanBookingId.startsWith(",")) {
            cleanBookingId = cleanBookingId.substring(1).trim();
        }
        if (cleanBookingId.endsWith(",")) {
            cleanBookingId = cleanBookingId.substring(0, cleanBookingId.length() - 1).trim();
        }
        
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied.");
            return "redirect:/admin/support";
        }
        
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(cleanBookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setPaymentStatus("CANCELLED");
            booking.setCampaignStatus("REJECTED");
            bookingRepository.save(booking);
            
            // Update payments
            List<Payment> payments = paymentRepository.findAllByBookingId(cleanBookingId);
            payments.stream()
                .filter(payment -> !payment.getDeleted())
                .forEach(payment -> {
                    payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                });
            
            // Send notification to user
            List<User> users = userRepository.findByEmail(booking.getEmail());
            if (!users.isEmpty()) {
                User user = users.get(0);
                Notification notification = new Notification(
                    user, "Booking Cancelled",
                    "Your booking " + cleanBookingId + " has been cancelled as requested.",
                    Notification.NotificationType.INFO
                );
                notificationRepository.save(notification);
            }
            
            // Log cancellation action
            CancellationAction action = new CancellationAction(
                cleanBookingId, "APPROVED", "Managing Director",
                booking.getFullName(), booking.getEmail(),
                booking.getTotalAmount().doubleValue(), booking.getServiceType().toString(),
                "User requested cancellation"
            );
            cancellationActionRepository.save(action);
            
            // Update ticket
            if (ticketId != null) {
                Optional<SupportRequest> ticketOpt = supportRequestRepository.findById(ticketId);
                if (ticketOpt.isPresent()) {
                    SupportRequest ticket = ticketOpt.get();
                    ticket.setStatus(SupportRequest.Status.IN_PROGRESS);
                    ticket.setExpressAction("CANCELLATION APPROVED");
                    supportRequestRepository.save(ticket);
                }
            }
            
            ra.addFlashAttribute("success", "Cancellation approved. Click 'Mark as Complete' to forward to CSO.");
        } else {
            ra.addFlashAttribute("error", "Booking not found: " + cleanBookingId);
        }
        
        return "redirect:/admin/support";
    }

    @PostMapping("/admin/managing-director/campaigns/cancel-reject-by-booking/{bookingId}")
    public String rejectCancellation(@PathVariable String bookingId, 
                                    @RequestParam(required = false) Long ticketId,
                                    HttpSession session, RedirectAttributes ra) {
        String cleanBookingId = bookingId.trim();
        if (cleanBookingId.startsWith(",")) {
            cleanBookingId = cleanBookingId.substring(1).trim();
        }
        if (cleanBookingId.endsWith(",")) {
            cleanBookingId = cleanBookingId.substring(0, cleanBookingId.length() - 1).trim();
        }
        
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied.");
            return "redirect:/admin/support";
        }
        
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(cleanBookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Send notification to user
            List<User> users = userRepository.findByEmail(booking.getEmail());
            if (!users.isEmpty()) {
                User user = users.get(0);
                Notification notification = new Notification(
                    user, "Cancellation Request Rejected",
                    "Your cancellation request for booking " + cleanBookingId + " has been rejected. The booking remains active.",
                    Notification.NotificationType.WARNING
                );
                notificationRepository.save(notification);
            }
            
            // Log cancellation rejection
            CancellationAction action = new CancellationAction(
                cleanBookingId, "REJECTED", "Managing Director",
                booking.getFullName(), booking.getEmail(),
                booking.getTotalAmount().doubleValue(), booking.getServiceType().toString(),
                "Cancellation request rejected by MD"
            );
            cancellationActionRepository.save(action);
            
            // Update ticket
            if (ticketId != null) {
                Optional<SupportRequest> ticketOpt = supportRequestRepository.findById(ticketId);
                if (ticketOpt.isPresent()) {
                    SupportRequest ticket = ticketOpt.get();
                    ticket.setStatus(SupportRequest.Status.IN_PROGRESS);
                    ticket.setExpressAction("CANCELLATION REJECTED");
                    supportRequestRepository.save(ticket);
                }
            }
            
            ra.addFlashAttribute("success", "Cancellation rejected. Click 'Mark as Complete' to forward to CSO.");
        } else {
            ra.addFlashAttribute("error", "Booking not found: " + cleanBookingId);
        }
        
        return "redirect:/admin/support";
    }
}
