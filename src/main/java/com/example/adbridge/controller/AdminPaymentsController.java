package com.example.adbridge.controller;

import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Payment;
import com.example.adbridge.model.PaymentAuditLog;
import com.example.adbridge.model.User;
import com.example.adbridge.model.AdminRole;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.PaymentRepository;
import com.example.adbridge.repo.UserRepository;
import com.example.adbridge.service.PaymentAuditService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/payments")
public class AdminPaymentsController {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentAuditService auditService;

    public AdminPaymentsController(PaymentRepository paymentRepository, 
                                 BookingRepository bookingRepository,
                                 UserRepository userRepository,
                                 PaymentAuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public String paymentsList(Model model,
                               @RequestParam(value = "status", required = false) Payment.PaymentStatus status,
                               @RequestParam(value = "method", required = false) Payment.PaymentMethod method,
                               @RequestParam(value = "search", required = false) String search,
                               HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view payments.");
            return "redirect:/admin/dashboard";
        }
        
        List<Payment> payments = new ArrayList<>(paymentRepository.findByDeletedFalseOrDeletedIsNull());
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim().toLowerCase();
            payments = payments.stream()
                    .filter(payment -> 
                        (payment.getPaymentId() != null && payment.getPaymentId().toLowerCase().contains(searchTerm)) ||
                        (payment.getBookingId() != null && payment.getBookingId().toLowerCase().contains(searchTerm)) ||
                        (payment.getAmount() != null && payment.getAmount().toString().contains(searchTerm)) ||
                        (payment.getPaymentStatus() != null && payment.getPaymentStatus().toString().toLowerCase().contains(searchTerm)) ||
                        (payment.getPaymentMethod() != null && payment.getPaymentMethod().toString().toLowerCase().contains(searchTerm))
                    )
                    .collect(Collectors.toList());
        }
        
        // Apply status filter
        if (status != null) {
            payments = payments.stream()
                    .filter(payment -> payment.getPaymentStatus() != null && 
                                payment.getPaymentStatus().equals(status))
                    .collect(Collectors.toList());
        }
        
        // Apply method filter
        if (method != null) {
            payments = payments.stream()
                    .filter(payment -> payment.getPaymentMethod() != null && 
                                payment.getPaymentMethod().equals(method))
                    .collect(Collectors.toList());
        }
        
        // Sort payments by creation date (most recent first)
        payments.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
        
        model.addAttribute("title", "Payments · Admin");
        model.addAttribute("pageHeading", "Payments");
        model.addAttribute("activeMenu", "payments");
        model.addAttribute("payments", payments);
        model.addAttribute("status", status);
        model.addAttribute("method", method);
        model.addAttribute("search", search);
        
        return "admin/payments/list";
    }

    @GetMapping("/{id}")
    public String paymentDetail(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view payments.");
            return "redirect:/admin/dashboard";
        }
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            return "redirect:/admin/payments";
        }
        
        Payment payment = paymentOpt.get();
        Optional<Booking> booking = bookingRepository.findByBookingId(payment.getBookingId());
        List<PaymentAuditLog> auditHistory = auditService.getPaymentHistory(id);
        
        model.addAttribute("title", "Payment " + payment.getPaymentId() + " · Admin");
        model.addAttribute("pageHeading", "Payment Details");
        model.addAttribute("activeMenu", "payments");
        model.addAttribute("payment", payment);
        model.addAttribute("booking", booking.orElse(null));
        model.addAttribute("auditHistory", auditHistory);
        
        return "admin/payments/detail";
    }

    @PostMapping("/{id}/confirm")
    public String confirmPayment(@PathVariable Long id, 
                                @RequestParam(value = "reason", required = false) String reason,
                                HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage payments.");
            return "redirect:/admin/dashboard";
        }
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/admin/payments";
        }

        Payment payment = paymentOpt.get();
        
        // Check if amount matches booking total (with tolerance)
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(payment.getBookingId());
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            if (booking.getTotalAmount() != null && payment.getAmount() != null) {
                if (!booking.getTotalAmount().equals(payment.getAmount())) {
                    if (reason == null || reason.trim().isEmpty()) {
                        ra.addFlashAttribute("error", "Amount mismatch detected. Please provide a reason for confirmation.");
                        return "redirect:/admin/payments/" + id;
                    }
                }
            }
        }
        
        // Check for bank transfer slip requirement
        if (payment.getPaymentMethod() == Payment.PaymentMethod.BANK_TRANSFER) {
            // TODO: Check if slip is attached via Documents module
            // For now, we'll skip this check
        }
        
        String currentUser = (String) session.getAttribute("SESSION_USER");
        if (currentUser == null) currentUser = "Unknown";
        
        Payment.PaymentStatus oldStatus = payment.getPaymentStatus();
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);
        
        // Update booking status - Finance Coordinator approves but everything stays PENDING until Managing Director approves
        bookingOpt.ifPresent(b -> {
            b.setPaymentStatus("PENDING");
            b.setCampaignStatus("PENDING");
            bookingRepository.save(b);
        });
        
        // Log audit trail
        auditService.logConfirmation(payment, currentUser);
        paymentRepository.save(payment); // Save audit fields
        
        return "redirect:/admin/payments";
    }

    @PostMapping("/{id}/cancel")
    public String cancelPayment(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage payments.");
            return "redirect:/admin/dashboard";
        }
        
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/admin/payments";
        }
        
        Payment payment = paymentOpt.get();
        String currentUser = (String) session.getAttribute("SESSION_USER");
        if (currentUser == null) currentUser = "Unknown";
        
        Payment.PaymentStatus oldStatus = payment.getPaymentStatus();
        payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        
        // Update booking status when payment is cancelled
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(payment.getBookingId());
        bookingOpt.ifPresent(b -> {
            b.setPaymentStatus("CANCELLED");
            b.setCampaignStatus("CANCELLED");
            bookingRepository.save(b);
        });
        
        // Log audit trail
        auditService.logCancellation(payment, currentUser, "Cancelled by Finance Coordinator");
        paymentRepository.save(payment); // Save audit fields
        
        return "redirect:/admin/payments";
    }

    @PostMapping("/{id}/refund")
    public String refundPayment(@PathVariable Long id,
                               @RequestParam("reason") String reason,
                               HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage payments.");
            return "redirect:/admin/dashboard";
        }
        if (reason == null || reason.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Reason is required for refund");
            return "redirect:/admin/payments/" + id;
        }
        
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/admin/payments";
        }
        
        Payment payment = paymentOpt.get();
        String currentUser = (String) session.getAttribute("SESSION_USER");
        if (currentUser == null) currentUser = "Unknown";
        
        Payment.PaymentStatus oldStatus = payment.getPaymentStatus();
        payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        
        // Update booking status
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(payment.getBookingId());
        bookingOpt.ifPresent(b -> {
            b.setPaymentStatus("REFUNDED");
            bookingRepository.save(b);
        });
        
        // Log audit trail
        auditService.logRefund(payment, currentUser, reason);
        paymentRepository.save(payment); // Save audit fields
        
        ra.addFlashAttribute("success", "Payment refunded successfully");
        return "redirect:/admin/payments";
    }

    @PostMapping("/{id}/delete")
    public String deletePayment(@PathVariable Long id,
                               @RequestParam("reason") String reason,
                               HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to manage payments.");
            return "redirect:/admin/dashboard";
        }
        if (reason == null || reason.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Reason is required for deletion");
            return "redirect:/admin/payments/" + id;
        }
        
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/admin/payments";
        }
        
        Payment payment = paymentOpt.get();
        String currentUser = (String) session.getAttribute("SESSION_USER");
        if (currentUser == null) currentUser = "Unknown";
        
        // Soft delete
        payment.setDeleted(true);
        payment.setDeletedAt(LocalDateTime.now());
        payment.setDeletedBy(currentUser);
        paymentRepository.save(payment);
        
        // Log audit trail
        auditService.logDeletion(payment, currentUser, reason);
        paymentRepository.save(payment); // Save audit fields
        
        ra.addFlashAttribute("success", "Payment deleted successfully");
        return "redirect:/admin/payments";
    }

    @GetMapping("/export/csv")
    public void exportCSV(@RequestParam(value = "status", required = false) Payment.PaymentStatus status,
                         @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                         @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                         HttpSession session, HttpServletResponse response) throws IOException {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            response.sendRedirect("/admin/dashboard?error=access_denied");
            return;
        }
        
        List<Payment> payments;
        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);
            if (status != null) {
                payments = paymentRepository.findByStatusAndDateRangeAndNotDeleted(status, start, end);
            } else {
                payments = paymentRepository.findByCreatedAtBetweenAndNotDeleted(start, end);
            }
        } else if (status != null) {
            payments = paymentRepository.findByPaymentStatusAndDeletedFalseOrDeletedIsNull(status);
        } else {
            payments = paymentRepository.findByDeletedFalseOrDeletedIsNull();
        }
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"payments_" + 
                          LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv\"");
        
        PrintWriter writer = response.getWriter();
        writer.println("Payment ID,Booking ID,Client Name,Method,Amount,Currency,Status,Date,External Txn ID");
        
        for (Payment payment : payments) {
            Optional<Booking> booking = bookingRepository.findByBookingId(payment.getBookingId());
            String clientName = booking.map(Booking::getFullName).orElse("N/A");
            
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"%n",
                payment.getPaymentId(),
                payment.getBookingId(),
                clientName,
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentStatus(),
                payment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                payment.getExternalTransactionId() != null ? payment.getExternalTransactionId() : ""
            );
        }
        
        writer.flush();
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPDF(@RequestParam(value = "status", required = false) Payment.PaymentStatus status,
                                          @RequestParam(value = "method", required = false) Payment.PaymentMethod method,
                                          @RequestParam(value = "search", required = false) String search,
                                          HttpSession session) {
        // Check if user has permission to access payments (Admin Assistant cannot access)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        
        List<Payment> payments = new ArrayList<>(paymentRepository.findByDeletedFalseOrDeletedIsNull());
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim().toLowerCase();
            payments = payments.stream()
                    .filter(payment -> 
                        (payment.getPaymentId() != null && payment.getPaymentId().toLowerCase().contains(searchTerm)) ||
                        (payment.getBookingId() != null && payment.getBookingId().toLowerCase().contains(searchTerm)) ||
                        (payment.getAmount() != null && payment.getAmount().toString().contains(searchTerm)) ||
                        (payment.getPaymentStatus() != null && payment.getPaymentStatus().toString().toLowerCase().contains(searchTerm)) ||
                        (payment.getPaymentMethod() != null && payment.getPaymentMethod().toString().toLowerCase().contains(searchTerm))
                    )
                    .collect(Collectors.toList());
        }
        
        // Apply status filter
        if (status != null) {
            payments = payments.stream()
                    .filter(payment -> payment.getPaymentStatus() != null && 
                                payment.getPaymentStatus().equals(status))
                    .collect(Collectors.toList());
        }
        
        if (method != null) {
            payments = payments.stream()
                    .filter(payment -> payment.getPaymentMethod() != null && 
                                payment.getPaymentMethod().equals(method))
                    .collect(Collectors.toList());
        }
        
        // Sort payments by creation date (most recent first)
        payments.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
        
        // Generate PDF using the existing PDF service pattern
        String html = generatePaymentsPdfHtml(payments);
        byte[] pdfBytes = convertHtmlToPdf(html);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payments_report.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private String generatePaymentsPdfHtml(List<Payment> payments) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Payments Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 10px; color: #333; line-height: 1.3; font-size: 10px; }");
        html.append(".report-container { width: 100%; background: white; padding: 0; }");
        html.append(".report-header { text-align: center; margin-bottom: 15px; border-bottom: 1px solid #2c3e50; padding-bottom: 10px; }");
        html.append(".report-title { font-size: 1.5rem; font-weight: bold; color: #2c3e50; margin: 0; }");
        html.append(".company-info { margin-top: 5px; color: #6c757d; font-size: 9px; }");
        html.append(".report-info { background: #f8f9fa; padding: 8px; border-radius: 3px; margin-bottom: 10px; font-size: 9px; }");
        html.append(".summary-grid { display: table; width: 100%; margin-bottom: 15px; }");
        html.append(".summary-card { display: table-cell; background: #fff; border: 1px solid #dee2e6; border-radius: 3px; padding: 8px; text-align: center; width: 25%; vertical-align: top; }");
        html.append(".summary-value { font-size: 14px; font-weight: bold; color: #2c3e50; margin: 3px 0; }");
        html.append(".summary-label { color: #6c757d; font-size: 8px; }");
        html.append(".highlight { background: #e8f5e8; border-color: #28a745; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 7px; page-break-inside: avoid; table-layout: fixed; }");
        html.append("th, td { border: 1px solid #ddd; padding: 2px 1px; text-align: left; vertical-align: top; word-wrap: break-word; overflow: hidden; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; color: #2c3e50; font-size: 6px; }");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        html.append("/* Column width constraints */");
        html.append("th:nth-child(1), td:nth-child(1) { width: 12%; }"); // Payment ID
        html.append("th:nth-child(2), td:nth-child(2) { width: 12%; }"); // Booking ID
        html.append("th:nth-child(3), td:nth-child(3) { width: 15%; }"); // Client Name
        html.append("th:nth-child(4), td:nth-child(4) { width: 10%; }"); // Method
        html.append("th:nth-child(5), td:nth-child(5) { width: 12%; }"); // Amount
        html.append("th:nth-child(6), td:nth-child(6) { width: 10%; }"); // Status
        html.append("th:nth-child(7), td:nth-child(7) { width: 15%; }"); // Date
        html.append("th:nth-child(8), td:nth-child(8) { width: 14%; }"); // External Txn ID
        html.append(".status-badge { padding: 1px 2px; border-radius: 1px; font-size: 5px; font-weight: bold; text-transform: uppercase; }");
        html.append(".status-completed { background-color: #d4edda; color: #155724; }");
        html.append(".status-pending { background-color: #fff3cd; color: #856404; }");
        html.append(".status-failed { background-color: #f8d7da; color: #721c24; }");
        html.append(".status-cancelled { background-color: #f8d7da; color: #721c24; }");
        html.append(".status-refunded { background-color: #d1ecf1; color: #0c5460; }");
        html.append(".footer { margin-top: 20px; text-align: center; color: #666; font-size: 8px; border-top: 1px solid #ddd; padding-top: 10px; }");
        html.append("</style></head><body>");
        
        html.append("<div class='report-container'>");
        html.append("<div class='report-header'>");
        html.append("<h1 class='report-title'>Payments Report</h1>");
        html.append("<div class='company-info'>AdBridgeLanka - Payment Management System</div>");
        html.append("</div>");
        
        html.append("<div class='report-info'>");
        html.append("<strong>Generated:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        html.append(" | <strong>Total Payments:</strong> ").append(payments.size());
        html.append("</div>");
        
        // Summary cards
        html.append("<div class='summary-grid'>");
        html.append("<div class='summary-card highlight'>");
        html.append("<div class='summary-value'>").append(payments.size()).append("</div>");
        html.append("<div class='summary-label'>Total Payments</div>");
        html.append("</div>");
        
        long completedCount = payments.stream().filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED).count();
        html.append("<div class='summary-card'>");
        html.append("<div class='summary-value'>").append(completedCount).append("</div>");
        html.append("<div class='summary-label'>Completed</div>");
        html.append("</div>");
        
        long pendingCount = payments.stream().filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.PENDING).count();
        html.append("<div class='summary-card'>");
        html.append("<div class='summary-value'>").append(pendingCount).append("</div>");
        html.append("<div class='summary-label'>Pending</div>");
        html.append("</div>");
        
        long totalAmount = payments.stream().mapToLong(p -> p.getAmount() != null ? p.getAmount() : 0).sum();
        html.append("<div class='summary-card'>");
        html.append("<div class='summary-value'>").append(String.format("%,d", totalAmount)).append(" LKR</div>");
        html.append("<div class='summary-label'>Total Amount</div>");
        html.append("</div>");
        html.append("</div>");
        
        // Separate payments by status
        List<Payment> pendingPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.PENDING)
                .collect(Collectors.toList());
        
        List<Payment> cancelledPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.CANCELLED)
                .collect(Collectors.toList());
        
        List<Payment> completedPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
                .collect(Collectors.toList());
        
        // PENDING Payments Table
        if (!pendingPayments.isEmpty()) {
            html.append("<h3 style='color: #856404; margin-top: 20px; margin-bottom: 10px; font-size: 12px;'>PENDING PAYMENTS (").append(pendingPayments.size()).append(")</h3>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Payment ID</th>");
            html.append("<th>Booking ID</th>");
            html.append("<th>Client Name</th>");
            html.append("<th>Method</th>");
            html.append("<th>Amount</th>");
            html.append("<th>Date</th>");
            html.append("<th>External Txn ID</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (Payment payment : pendingPayments) {
                Optional<Booking> booking = bookingRepository.findByBookingId(payment.getBookingId());
                String clientName = booking.map(Booking::getFullName).orElse("N/A");
                
                html.append("<tr>");
                html.append("<td>").append(payment.getPaymentId() != null ? payment.getPaymentId() : "N/A").append("</td>");
                html.append("<td>").append(payment.getBookingId() != null ? payment.getBookingId() : "N/A").append("</td>");
                html.append("<td>").append(clientName).append("</td>");
                html.append("<td>").append(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "N/A").append("</td>");
                html.append("<td>").append(payment.getAmount() != null ? String.format("%,d %s", payment.getAmount(), payment.getCurrency() != null ? payment.getCurrency() : "LKR") : "N/A").append("</td>");
                html.append("<td>").append(payment.getCreatedAt() != null ? payment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "N/A").append("</td>");
                html.append("<td>").append(payment.getExternalTransactionId() != null ? payment.getExternalTransactionId() : "N/A").append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
        }
        
        // CANCELLED Payments Table
        if (!cancelledPayments.isEmpty()) {
            html.append("<h3 style='color: #721c24; margin-top: 20px; margin-bottom: 10px; font-size: 12px;'>CANCELLED PAYMENTS (").append(cancelledPayments.size()).append(")</h3>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Payment ID</th>");
            html.append("<th>Booking ID</th>");
            html.append("<th>Client Name</th>");
            html.append("<th>Method</th>");
            html.append("<th>Amount</th>");
            html.append("<th>Date</th>");
            html.append("<th>External Txn ID</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (Payment payment : cancelledPayments) {
                Optional<Booking> booking = bookingRepository.findByBookingId(payment.getBookingId());
                String clientName = booking.map(Booking::getFullName).orElse("N/A");
                
                html.append("<tr>");
                html.append("<td>").append(payment.getPaymentId() != null ? payment.getPaymentId() : "N/A").append("</td>");
                html.append("<td>").append(payment.getBookingId() != null ? payment.getBookingId() : "N/A").append("</td>");
                html.append("<td>").append(clientName).append("</td>");
                html.append("<td>").append(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "N/A").append("</td>");
                html.append("<td>").append(payment.getAmount() != null ? String.format("%,d %s", payment.getAmount(), payment.getCurrency() != null ? payment.getCurrency() : "LKR") : "N/A").append("</td>");
                html.append("<td>").append(payment.getCreatedAt() != null ? payment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "N/A").append("</td>");
                html.append("<td>").append(payment.getExternalTransactionId() != null ? payment.getExternalTransactionId() : "N/A").append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
        }
        
        // COMPLETED Payments Table
        if (!completedPayments.isEmpty()) {
            html.append("<h3 style='color: #155724; margin-top: 20px; margin-bottom: 10px; font-size: 12px;'>COMPLETED PAYMENTS (").append(completedPayments.size()).append(")</h3>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Payment ID</th>");
            html.append("<th>Booking ID</th>");
            html.append("<th>Client Name</th>");
            html.append("<th>Method</th>");
            html.append("<th>Amount</th>");
            html.append("<th>Date</th>");
            html.append("<th>External Txn ID</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (Payment payment : completedPayments) {
                Optional<Booking> booking = bookingRepository.findByBookingId(payment.getBookingId());
                String clientName = booking.map(Booking::getFullName).orElse("N/A");
                
                html.append("<tr>");
                html.append("<td>").append(payment.getPaymentId() != null ? payment.getPaymentId() : "N/A").append("</td>");
                html.append("<td>").append(payment.getBookingId() != null ? payment.getBookingId() : "N/A").append("</td>");
                html.append("<td>").append(clientName).append("</td>");
                html.append("<td>").append(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "N/A").append("</td>");
                html.append("<td>").append(payment.getAmount() != null ? String.format("%,d %s", payment.getAmount(), payment.getCurrency() != null ? payment.getCurrency() : "LKR") : "N/A").append("</td>");
                html.append("<td>").append(payment.getCreatedAt() != null ? payment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "N/A").append("</td>");
                html.append("<td>").append(payment.getExternalTransactionId() != null ? payment.getExternalTransactionId() : "N/A").append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
        }
        html.append("</div>");
        
        html.append("<div class='footer'>");
        html.append("This report was generated automatically by AdBridgeLanka Payment Management System<br>");
        html.append("For support, contact the Finance Coordinator");
        html.append("</div>");
        
        html.append("</body></html>");
        return html.toString();
    }

    private byte[] convertHtmlToPdf(String html) {
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            com.itextpdf.html2pdf.HtmlConverter.convertToPdf(html, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}