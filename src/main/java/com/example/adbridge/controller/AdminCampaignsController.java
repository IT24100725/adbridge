package com.example.adbridge.controller;

import com.example.adbridge.model.AdminRole;
import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Payment;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.PaymentRepository;
import com.example.adbridge.service.CampaignPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class AdminCampaignsController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final CampaignPdfService campaignPdfService;

    public AdminCampaignsController(BookingRepository bookingRepository, PaymentRepository paymentRepository, CampaignPdfService campaignPdfService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.campaignPdfService = campaignPdfService;
    }

    @GetMapping("/admin/campaigns")
    public String campaignsList(@RequestParam(value = "filter", required = false) String filter,
                               @RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "sortBy", required = false) String sortBy,
                               Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view campaigns.");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("title", "Campaigns · Admin");
        model.addAttribute("pageHeading", "Campaigns");
        model.addAttribute("activeMenu", "campaigns");

        var all = bookingRepository.findAll().stream()
                .filter(b -> {
                    String campaignStatus = b.getCampaignStatus();

                    // Only allow bookings that are in one of these specific states
                    return "PENDING".equals(campaignStatus) ||
                            "REJECTED".equals(campaignStatus) ||
                            "APPROVED".equals(campaignStatus);
                })
                .toList();

        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim().toLowerCase();
            all = all.stream()
                    .filter(b -> 
                        (b.getFullName() != null && b.getFullName().toLowerCase().contains(searchTerm)) ||
                        (b.getCompanyName() != null && b.getCompanyName().toLowerCase().contains(searchTerm)) ||
                        (b.getBookingId() != null && b.getBookingId().toLowerCase().contains(searchTerm)) ||
                        (b.getServiceType() != null && b.getServiceType().toString().toLowerCase().contains(searchTerm)) ||
                        (b.getCampaignStatus() != null && b.getCampaignStatus().toLowerCase().contains(searchTerm))
                    )
                    .toList();
        }

        // Apply status filter
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            // Filter by specific status
            all = all.stream()
                    .filter(b -> b.getCampaignStatus() != null && 
                                b.getCampaignStatus().equalsIgnoreCase(sortBy))
                    .toList();
        }
        // If sortBy is null or empty, show all statuses (no filtering)

        if (filter != null && filter.equalsIgnoreCase("expired")) {
            java.time.LocalDate today = java.time.LocalDate.now();
            all = all.stream().filter(b -> b.getLastModifiedAt() != null && b.getLastModifiedAt().toLocalDate().isBefore(today.minusDays(30))).toList();
        }
        
        model.addAttribute("campaigns", all);
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        return "admin/campaigns/list";
    }

    @PostMapping("/admin/campaigns/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage campaigns.");
            return "redirect:/admin/dashboard";
        }
        bookingRepository.findById(id).ifPresent(b -> {
            b.setPaymentStatus("APPROVED");
            bookingRepository.save(b);
        });
        return "redirect:/admin/campaigns";
    }

    // Scaffold routes for new/detail pages (structure only)
    @GetMapping("/admin/campaigns/new")
    public String newCampaign(HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view campaigns.");
            return "redirect:/admin/dashboard";
        }
        return "admin/campaigns/form";
    }

    @GetMapping("/admin/campaigns/{id}")
    public String detail(@PathVariable Long id, HttpSession session, RedirectAttributes ra, Model model) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view campaigns.");
            return "redirect:/admin/dashboard";
        }

        // Load campaign/booking data
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Campaign not found.");
            return "redirect:/admin/campaigns";
        }

        Booking campaign = bookingOpt.get();
        model.addAttribute("campaign", campaign);

        // Load payment data - get the most recent payment if multiple exist
        List<Payment> payments = paymentRepository.findAllByBookingId(campaign.getBookingId());
        Optional<Payment> paymentOpt = payments.stream()
                .filter(payment -> payment.getDeleted() == null || !payment.getDeleted()) // Exclude deleted payments (handle null)
                .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt())); // Get most recent
        if (paymentOpt.isPresent()) {
            model.addAttribute("payment", paymentOpt.get());
        }

        model.addAttribute("title", "Campaign Details · Admin");
        model.addAttribute("pageHeading", "Campaign Details");
        model.addAttribute("activeMenu", "campaigns");

        return "admin/campaigns/detail";
    }

    @PostMapping("/admin/campaigns/{id}/reject")
    public String reject(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage campaigns.");
            return "redirect:/admin/dashboard";
        }
        bookingRepository.findById(id).ifPresent(b -> {
            b.setPaymentStatus("REJECTED");
            bookingRepository.save(b);
        });
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/campaigns/{id}/pipeline")
    public String updatePipeline(@PathVariable Long id,
                                 @RequestParam String status,
                                 @RequestParam(required = false) Integer progress,
                                 HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage campaigns.");
            return "redirect:/admin/dashboard";
        }
        bookingRepository.findById(id).ifPresent(b -> {
            b.setPaymentStatus(status);
            if (progress != null) {
                b.setCurrentVersion(Math.max(1, Math.min(100, progress)));
            }
            bookingRepository.save(b);
        });
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/campaigns/{id}/archive")
    public String archive(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage campaigns.");
            return "redirect:/admin/dashboard";
        }
        bookingRepository.findById(id).ifPresent(b -> {
            b.setPaymentStatus("COMPLETED");
            b.setCurrentVersion(100);
            bookingRepository.save(b);
        });
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/campaigns/{id}/create-campaign")
    public String createCampaign(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to create campaigns (only Marketing and Super Admin)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.MARKETING && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Marketing Planner can create campaigns.");
            return "redirect:/admin/campaigns";
        }

        bookingRepository.findById(id).ifPresent(b -> {
            // Only allow campaign creation for Finance Coordinator approved payments
            if (!"PENDING".equals(b.getPaymentStatus()) || !"PENDING".equals(b.getCampaignStatus())) {
                ra.addFlashAttribute("error", "Cannot create campaign. Payment must be approved by Finance Coordinator first.");
                return;
            }

            // Create campaign - everything stays PENDING until Managing Director approves
            b.setPaymentStatus("PENDING");
            b.setCampaignStatus("PENDING");
            b.setCurrentVersion(0); // Start at 0% progress
            bookingRepository.save(b);
        });

        ra.addFlashAttribute("success", "Campaign created successfully and is pending approval.");
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/campaigns/{id}/reject-campaign")
    public String rejectCampaign(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to reject campaigns (only Marketing and Super Admin)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.MARKETING && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Marketing Planner can reject campaigns.");
            return "redirect:/admin/campaigns";
        }

        bookingRepository.findById(id).ifPresent(b -> {
            // Marketing Planner rejects campaign - both payment and campaign status become REJECTED
            b.setPaymentStatus("REJECTED");
            b.setCampaignStatus("REJECTED");
            b.setCurrentVersion(0); // Reset progress
            bookingRepository.save(b);

            // Also update Payment entity to CANCELLED when Marketing Planner rejects
            // Update all non-deleted payments for this booking
            List<Payment> payments = paymentRepository.findAllByBookingId(b.getBookingId());
            payments.stream()
                    .filter(payment -> payment.getDeleted() == null || !payment.getDeleted())
                    .forEach(payment -> {
                        payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
                        paymentRepository.save(payment);
                    });
        });

        ra.addFlashAttribute("success", "Campaign rejected successfully.");
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/admin/campaigns/pdf/approved")
    public ResponseEntity<byte[]> downloadApprovedCampaignsPdf(HttpSession session) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        // Get approved campaigns (APPROVED status)
        var approvedCampaigns = bookingRepository.findAll().stream()
                .filter(b -> "APPROVED".equals(b.getCampaignStatus()) && "COMPLETED".equals(b.getPaymentStatus()))
                .toList();

        byte[] pdfBytes = campaignPdfService.generateApprovedCampaignsReport(approvedCampaigns);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "approved_campaigns_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/admin/campaigns/pdf/rejected")
    public ResponseEntity<byte[]> downloadRejectedCampaignsPdf(HttpSession session) {
        // Check if user has permission to access campaigns (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        // Get rejected campaigns (REJECTED status)
        var rejectedCampaigns = bookingRepository.findAll().stream()
                .filter(b -> "REJECTED".equals(b.getCampaignStatus()))
                .toList();

        byte[] pdfBytes = campaignPdfService.generateRejectedCampaignsReport(rejectedCampaigns);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rejected_campaigns_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
