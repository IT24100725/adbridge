package com.example.adbridge.controller;

import com.example.adbridge.model.Contact;
import com.example.adbridge.model.SupportRequest;
import com.example.adbridge.model.SupportComment;
import com.example.adbridge.model.AdminRole;
import com.example.adbridge.model.ExpressBookingAction;
import com.example.adbridge.model.CancellationAction;
import com.example.adbridge.repo.SupportRequestRepository;
import com.example.adbridge.repo.ContactRepository;
import com.example.adbridge.repo.ExpressBookingActionRepository;
import com.example.adbridge.repo.CancellationActionRepository;
import com.example.adbridge.service.NotificationService;
import com.example.adbridge.repo.SupportCommentRepository;
import com.example.adbridge.repo.UserRepository;
import com.example.adbridge.repo.NotificationRepository;
import com.example.adbridge.model.User;
import com.example.adbridge.model.Notification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/support")
public class AdminSupportController {

    private final SupportRequestRepository supportRepository;
    private final SupportCommentRepository commentRepository;
    private final ContactRepository contactRepository;
    private final NotificationService notificationService;
    private final ExpressBookingActionRepository expressBookingActionRepository;
    private final CancellationActionRepository cancellationActionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public AdminSupportController(SupportRequestRepository supportRepository, SupportCommentRepository commentRepository, ContactRepository contactRepository, NotificationService notificationService, ExpressBookingActionRepository expressBookingActionRepository, CancellationActionRepository cancellationActionRepository, UserRepository userRepository, NotificationRepository notificationRepository) {
        this.supportRepository = supportRepository;
        this.commentRepository = commentRepository;
        this.contactRepository = contactRepository;
        this.notificationService = notificationService;
        this.expressBookingActionRepository = expressBookingActionRepository;
        this.cancellationActionRepository = cancellationActionRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "status", required = false) SupportRequest.Status status,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "sortBy", required = false) String sortBy,
                       Model model, HttpSession session) {
        List<SupportRequest> tickets = (status == null)
                ? supportRepository.findAll()
                : supportRepository.findByStatusOrderByCreatedAtDesc(status);
        
        // Get current user role for permission checking
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole == null) currentRole = AdminRole.ADMIN;
        
        // Filter tickets for Managing Director - only show tickets assigned to Director/Managing Director
        if (currentRole == AdminRole.DIRECTOR) {
            tickets = tickets.stream()
                    .filter(t -> "Director".equals(t.getAssignedTo()) || "Managing Director".equals(t.getAssignedTo()))
                    .toList();
        }
        
        // For CSO (SUPPORT role), also include completed express tickets
        if (currentRole == AdminRole.SUPPORT) {
            List<SupportRequest> completedExpressTickets = supportRepository.findAll().stream()
                    .filter(t -> t.getStatus() == SupportRequest.Status.RESOLVED && 
                                (t.getExpressAction() != null && !t.getExpressAction().isEmpty()))
                    .toList();
            tickets.addAll(completedExpressTickets);
        }
        
        if (q != null && !q.trim().isEmpty()) {
            String query = q.trim().toLowerCase();
            tickets = tickets.stream().filter(t ->
                    (t.getClientName() != null && t.getClientName().toLowerCase().contains(query)) ||
                    (t.getClientEmail() != null && t.getClientEmail().toLowerCase().contains(query)) ||
                    (t.getSubject() != null && t.getSubject().toLowerCase().contains(query)) ||
                    (t.getMessage() != null && t.getMessage().toLowerCase().contains(query)) ||
                    (t.getBookingId() != null && t.getBookingId().toLowerCase().contains(query))
            ).toList();
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            switch (sortBy.toLowerCase()) {
                case "priority_high":
                    tickets = tickets.stream()
                            .filter(t -> t.getPriority() == SupportRequest.Priority.HIGH)
                            .toList();
                    break;
                case "priority_medium":
                    tickets = tickets.stream()
                            .filter(t -> t.getPriority() == SupportRequest.Priority.MEDIUM)
                            .toList();
                    break;
                case "priority_low":
                    tickets = tickets.stream()
                            .filter(t -> t.getPriority() == SupportRequest.Priority.LOW)
                            .toList();
                    break;
            }
        }
        
        // Get staff list for assignment dropdown
        List<String> staffList = List.of(
            "Support Officer", "Admin Assistant", "Finance Coordinator", 
            "Marketing Manager", "Director", "Super Admin"
        );
        
        // Separate cancel bookings from re-approval requests
        List<Contact> cancelBookings = contactRepository.findByIsRepliedFalseOrderByCreatedAtDesc().stream()
                .filter(c -> "CANCEL_BOOKING".equals(c.getRequestType()))
                .toList();

        List<Contact> expressBookings = contactRepository.findByIsRepliedFalseOrderByCreatedAtDesc().stream()
                .filter(c -> "REJECTED_BOOKING_REQUEST".equals(c.getRequestType()))
                .toList();
        
        model.addAttribute("title", "Support Requests · Admin");
        model.addAttribute("pageHeading", "Support Requests");
        model.addAttribute("activeMenu", "support");
        model.addAttribute("tickets", tickets);
        model.addAttribute("cancelBookings", cancelBookings);
        model.addAttribute("expressBookings", expressBookings);
        model.addAttribute("q", q);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("adminRole", currentRole);
        model.addAttribute("staffList", staffList);
        return "admin/support/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long contactId, Model model, HttpSession session) {
        System.out.println("=== SUPPORT FORM DEBUG ===");
        System.out.println("contactId: " + contactId);
        if (contactId != null) {
            Optional<Contact> contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isPresent()) {
                Contact contact = contactOpt.get();
                System.out.println("Contact found: " + contact.getName() + " - " + contact.getEmail());
                model.addAttribute("contact", contact);
                model.addAttribute("prefillBookingId", contact.getBookingId());
                model.addAttribute("prefillSubject", 
                    contact.getRequestType().equals("CANCEL_BOOKING") ? 
                    "Booking Cancellation: " + contact.getBookingId() : 
                    "Re-approval Request: " + contact.getBookingId());
                model.addAttribute("prefillMessage", contact.getRequestReason());
                model.addAttribute("prefillClientName", contact.getName());
                model.addAttribute("prefillClientEmail", contact.getEmail());
                model.addAttribute("contactId", contactId);
                
                System.out.println("Prefill values set:");
                System.out.println("- prefillBookingId: " + contact.getBookingId());
                System.out.println("- prefillSubject: " + (contact.getRequestType().equals("CANCEL_BOOKING") ? "Booking Cancellation: " + contact.getBookingId() : "Re-approval Request: " + contact.getBookingId()));
                System.out.println("- prefillMessage: " + contact.getRequestReason());
                System.out.println("- prefillClientName: " + contact.getName());
                System.out.println("- prefillClientEmail: " + contact.getEmail());
            } else {
                System.out.println("Contact not found for ID: " + contactId);
            }
        } else {
            System.out.println("No contactId provided");
        }
        // Get staff list for assignment dropdown
        List<String> staffList = List.of(
            "Support Officer", "Admin Assistant", "Finance Coordinator", 
            "Marketing Manager", "Director", "Super Admin"
        );
        
        model.addAttribute("title", "New Support Request · Admin");
        model.addAttribute("activeMenu", "support");
        model.addAttribute("ticket", new SupportRequest());
        model.addAttribute("staffList", staffList);
        return "admin/support/form";
    }

    @PostMapping
    public String create(@ModelAttribute SupportRequest ticket, 
                        @RequestParam(required = false) Long contactId,
                        RedirectAttributes ra) {
        if (ticket.getClientName() == null || ticket.getClientName().trim().isEmpty() ||
                ticket.getClientEmail() == null || ticket.getClientEmail().trim().isEmpty()) {
            ra.addFlashAttribute("error", "Client name and email are required");
            return "redirect:/admin/support/new";
        }
        supportRepository.save(ticket);
        
        // If this ticket was created from a contact request, mark the contact as having a ticket created
        if (contactId != null) {
            Optional<Contact> contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isPresent()) {
                Contact contact = contactOpt.get();
                contact.setTicketCreated(true);
                contactRepository.save(contact);
                System.out.println("Marked contact " + contactId + " as having ticket created");
            }
        }
        
        // notify client (placeholder)
        notificationService.sendEmail(ticket.getClientEmail(), "We received your request", "Hello " + ticket.getClientName() + ",\n\nYour support ticket has been created. We'll get back to you soon.\n\nThanks.");
        
        // Create in-app notification for the client if they have a user account
        try {
            List<User> users = userRepository.findByEmail(ticket.getClientEmail());
            User clientUser = users.isEmpty() ? null : users.get(0);
            if (clientUser != null) {
                // Create notification based on the request type
                String notificationTitle;
                String notificationMessage;
                
                // Determine request type from ticket subject or message
                String subject = ticket.getSubject() != null ? ticket.getSubject().toLowerCase() : "";
                String message = ticket.getMessage() != null ? ticket.getMessage().toLowerCase() : "";
                
                if (subject.contains("client") || message.contains("create") || message.contains("client")) {
                    notificationTitle = "Create New Client Request - Processing";
                    notificationMessage = "Your client creation request is now being processed by our support team. We'll update you on the progress soon.";
                } else if (subject.contains("cancel") || message.contains("cancel")) {
                    notificationTitle = "Cancel Booking Request - Processing";
                    notificationMessage = "Your booking cancellation request is now being processed by our support team. We'll update you on the progress soon.";
                } else if (subject.contains("reject") || message.contains("reject") || subject.contains("re-approval")) {
                    notificationTitle = "Rejected Booking Request - Processing";
                    notificationMessage = "Your re-approval request is now being processed by our support team. We'll update you on the progress soon.";
                } else {
                    notificationTitle = "General Inquiry - Processing";
                    notificationMessage = "Your inquiry is now being processed by our support team. We'll get back to you soon.";
                }
                
                Notification notification = new Notification(
                    clientUser,
                    notificationTitle,
                    notificationMessage,
                    Notification.NotificationType.INFO
                );
                notificationRepository.save(notification);
                
                System.out.println("Created support request notification for user: " + clientUser.getEmail());
            }
        } catch (Exception e) {
            System.out.println("Could not create support request notification: " + e.getMessage());
        }
        
        ra.addFlashAttribute("success", "Support request created");
        return "redirect:/admin/support";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra, HttpSession session) {
        Optional<SupportRequest> t = supportRepository.findById(id);
        if (t.isEmpty()) {
            ra.addFlashAttribute("error", "Support request not found");
            return "redirect:/admin/support";
        }
        
        // Check if user has permission to edit (Admin Assistant can only view and comment)
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole == null) currentRole = AdminRole.ADMIN;
        
        // Get staff list for assignment dropdown
        List<String> staffList = List.of(
            "Support Officer", "Admin Assistant", "Finance Coordinator", 
            "Marketing Manager", "Director", "Super Admin"
        );
        
        model.addAttribute("title", "Edit Support Request · Admin");
        model.addAttribute("activeMenu", "support");
        model.addAttribute("ticket", t.get());
        model.addAttribute("comments", commentRepository.findBySupportRequestIdOrderByCreatedAtAsc(id));
        model.addAttribute("adminRole", currentRole);
        model.addAttribute("staffList", staffList);
        return "admin/support/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute SupportRequest updated, RedirectAttributes ra, HttpSession session) {
        // Check if user has permission to edit (Admin Assistant cannot edit)
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole == null) currentRole = AdminRole.ADMIN;
        
        if (currentRole == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "You don't have permission to edit support requests");
            return "redirect:/admin/support";
        }
        Optional<SupportRequest> t = supportRepository.findById(id);
        if (t.isEmpty()) {
            ra.addFlashAttribute("error", "Support request not found");
            return "redirect:/admin/support";
        }
        SupportRequest s = t.get();
        s.setClientName(updated.getClientName());
        s.setClientEmail(updated.getClientEmail());
        s.setSubject(updated.getSubject());
        s.setMessage(updated.getMessage());
        s.setStatus(updated.getStatus());
        s.setPriority(updated.getPriority());
        s.setAssignedTo(updated.getAssignedTo());
        s.setBookingId(updated.getBookingId());
        supportRepository.save(s);
        // notify client on status change
        if (updated.getStatus() != null && s.getClientEmail() != null) {
            notificationService.sendEmail(s.getClientEmail(), "Ticket status updated", "Your ticket status is now: " + s.getStatus());
            
            // Create in-app notification for status change
            try {
                List<User> users = userRepository.findByEmail(s.getClientEmail());
                User clientUser = users.isEmpty() ? null : users.get(0);
                if (clientUser != null) {
                    String notificationTitle = "Support Request Status Update";
                    String notificationMessage = "Your support request status has been updated to: " + s.getStatus();
                    
                    Notification notification = new Notification(
                        clientUser,
                        notificationTitle,
                        notificationMessage,
                        Notification.NotificationType.INFO
                    );
                    notificationRepository.save(notification);
                    
                    System.out.println("Created status update notification for user: " + clientUser.getEmail());
                }
            } catch (Exception e) {
                System.out.println("Could not create status update notification: " + e.getMessage());
            }
        }
        ra.addFlashAttribute("success", "Support request updated");
        return "redirect:/admin/support";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String author,
                             @RequestParam String content,
                             RedirectAttributes ra) {
        Optional<SupportRequest> t = supportRepository.findById(id);
        if (t.isEmpty()) {
            ra.addFlashAttribute("error", "Support request not found");
            return "redirect:/admin/support";
        }
        if (content == null || content.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Comment content required");
            return "redirect:/admin/support/" + id + "/edit";
        }
        SupportComment c = new SupportComment();
        c.setSupportRequestId(id);
        c.setAuthor(author == null || author.isBlank() ? "support" : author.trim());
        c.setContent(content.trim());
        commentRepository.save(c);
        
        // Create notification for the client if they have a user account
        t.ifPresent(ticket -> {
            // Send email notification
            notificationService.sendEmail(ticket.getClientEmail(), "Update on your ticket", c.getContent());
            
            // Try to find the user by email and create in-app notification
            try {
                List<User> users = userRepository.findByEmail(ticket.getClientEmail());
                User clientUser = users.isEmpty() ? null : users.get(0);
                if (clientUser != null) {
                    // Create notification based on the original request type
                    String notificationTitle;
                    String notificationMessage;
                    
                    // Determine request type from ticket subject or message
                    String subject = ticket.getSubject() != null ? ticket.getSubject().toLowerCase() : "";
                    String message = ticket.getMessage() != null ? ticket.getMessage().toLowerCase() : "";
                    
                    if (subject.contains("client") || message.contains("create") || message.contains("client")) {
                        notificationTitle = "Create New Client Request - Reply";
                        notificationMessage = "Our support team has replied to your client creation request: " + c.getContent();
                    } else if (subject.contains("cancel") || message.contains("cancel")) {
                        notificationTitle = "Cancel Booking Request - Reply";
                        notificationMessage = "Our support team has replied to your booking cancellation request: " + c.getContent();
                    } else if (subject.contains("reject") || message.contains("reject") || subject.contains("re-approval")) {
                        notificationTitle = "Rejected Booking Request - Reply";
                        notificationMessage = "Our support team has replied to your re-approval request: " + c.getContent();
                    } else {
                        notificationTitle = "General Inquiry - Reply";
                        notificationMessage = "Our support team has replied to your inquiry: " + c.getContent();
                    }
                    
                    Notification notification = new Notification(
                        clientUser,
                        notificationTitle,
                        notificationMessage,
                        Notification.NotificationType.INFO
                    );
                    notificationRepository.save(notification);
                    
                    System.out.println("Created notification for user: " + clientUser.getEmail());
                } else {
                    System.out.println("No user found with email: " + ticket.getClientEmail());
                }
            } catch (Exception e) {
                System.out.println("Could not create in-app notification: " + e.getMessage());
            }
        });
        
        ra.addFlashAttribute("success", "Comment added");
        return "redirect:/admin/support/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        // Check if user has permission to delete (Admin Assistant cannot delete)
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole == null) currentRole = AdminRole.ADMIN;
        
        if (currentRole == AdminRole.ADMIN) {
            ra.addFlashAttribute("error", "You don't have permission to delete support requests");
            return "redirect:/admin/support";
        }
        if (supportRepository.existsById(id)) {
            supportRepository.deleteById(id);
            ra.addFlashAttribute("success", "Support request deleted");
        } else {
            ra.addFlashAttribute("error", "Support request not found");
        }
        return "redirect:/admin/support";
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes ra) {
        Optional<SupportRequest> ticketOpt = supportRepository.findById(id);
        if (ticketOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Support request not found");
            return "redirect:/admin/support";
        }

        SupportRequest ticket = ticketOpt.get();
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        
        // Check if Managing Director can view this ticket (only if assigned to Director/Managing Director)
        if (currentRole == AdminRole.DIRECTOR) {
            if (!"Director".equals(ticket.getAssignedTo()) && !"Managing Director".equals(ticket.getAssignedTo())) {
                ra.addFlashAttribute("error", "Access denied. You can only view tickets assigned to you.");
                return "redirect:/admin/support";
            }
        }

        // Get comments for this ticket
        List<SupportComment> comments = commentRepository.findBySupportRequestIdOrderByCreatedAtAsc(id);
        
        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);
        model.addAttribute("adminRole", currentRole);
        model.addAttribute("title", "Ticket Details · Admin");
        model.addAttribute("pageHeading", "Ticket Details");
        model.addAttribute("activeMenu", "support");
        
        return "admin/support/detail";
    }

    @PostMapping("/{id}/resolve")
    public String resolveTicket(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (currentRole != AdminRole.DIRECTOR && currentRole != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Managing Director can resolve tickets.");
            return "redirect:/admin/support";
        }

        Optional<SupportRequest> ticketOpt = supportRepository.findById(id);
        if (ticketOpt.isPresent()) {
            SupportRequest ticket = ticketOpt.get();
            ticket.setStatus(SupportRequest.Status.RESOLVED);
            supportRepository.save(ticket);
            
            // Create notification for ticket resolution
            try {
                List<User> users = userRepository.findByEmail(ticket.getClientEmail());
                User clientUser = users.isEmpty() ? null : users.get(0);
                if (clientUser != null) {
                    String notificationTitle = "Support Request Resolved";
                    String notificationMessage = "Your support request has been resolved. Thank you for contacting us!";
                    
                    Notification notification = new Notification(
                        clientUser,
                        notificationTitle,
                        notificationMessage,
                        Notification.NotificationType.SUCCESS
                    );
                    notificationRepository.save(notification);
                    
                    System.out.println("Created resolution notification for user: " + clientUser.getEmail());
                }
            } catch (Exception e) {
                System.out.println("Could not create resolution notification: " + e.getMessage());
            }
            
            ra.addFlashAttribute("success", "Ticket marked as resolved");
        } else {
            ra.addFlashAttribute("error", "Ticket not found");
        }
        return "redirect:/admin/support/" + id;
    }
    
    @PostMapping("/send-notification/{ticketId}")
    public String sendNotification(@PathVariable Long ticketId, HttpSession session, RedirectAttributes ra) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied.");
            return "redirect:/admin/support";
        }
        
        Optional<SupportRequest> ticketOpt = supportRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportRequest ticket = ticketOpt.get();
            
            // Send notification to client
            String subject = "Booking " + ticket.getExpressAction() + " - " + ticket.getBookingId();
            String message = "Your booking " + ticket.getBookingId() + " has been " + 
                           ticket.getExpressAction().toLowerCase() + " by the Managing Director.";
            
            // Update ticket notification status
            ticket.setNotificationSent(true);
            ticket.setNotificationSentAt(java.time.LocalDateTime.now());
            supportRepository.save(ticket);
            
            // Here you would typically send email/SMS notification
            // For now, we'll just log it
            System.out.println("=== NOTIFICATION SENT ===");
            System.out.println("To: " + ticket.getClientEmail());
            System.out.println("Subject: " + subject);
            System.out.println("Message: " + message);
            
            ra.addFlashAttribute("success", "Notification sent to client.");
        } else {
            ra.addFlashAttribute("error", "Ticket not found.");
        }
        
        return "redirect:/admin/support";
    }
    
    @GetMapping("/reports/express-bookings")
    public ResponseEntity<String> downloadExpressBookingsReport(HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT && role != AdminRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).body("Access denied.");
        }
        
        List<ExpressBookingAction> actions = expressBookingActionRepository.findAllByOrderByCreatedAtDesc();
        
        StringBuilder csv = new StringBuilder();
        csv.append("Booking ID,Action,Performed By,Client Name,Client Email,Total Amount,Service Type,Created At\n");
        
        for (ExpressBookingAction action : actions) {
            csv.append(action.getBookingId()).append(",")
               .append(action.getAction()).append(",")
               .append(action.getPerformedBy()).append(",")
               .append(action.getClientName()).append(",")
               .append(action.getClientEmail()).append(",")
               .append(action.getTotalAmount()).append(",")
               .append(action.getServiceType()).append(",")
               .append(action.getCreatedAt()).append("\n");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "express_bookings_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString());
    }

    @GetMapping("/reports/cancellations")
    public ResponseEntity<String> downloadCancellationsReport(HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT && role != AdminRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).body("Access denied.");
        }
        
        List<CancellationAction> actions = cancellationActionRepository.findAllByOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("Booking ID,Action,Performed By,Client Name,Client Email,Total Amount,Service Type,Cancellation Reason,Created At\n");
        
        for (CancellationAction action : actions) {
            csv.append(action.getBookingId()).append(",")
               .append(action.getAction()).append(",")
               .append(action.getPerformedBy()).append(",")
               .append(action.getClientName()).append(",")
               .append(action.getClientEmail()).append(",")
               .append(action.getTotalAmount()).append(",")
               .append(action.getServiceType()).append(",")
               .append(action.getCancellationReason()).append(",")
               .append(action.getCreatedAt()).append("\n");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "cancellation_actions_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString());
    }

    @GetMapping("/reports/express-bookings/pdf")
    public ResponseEntity<byte[]> downloadExpressBookingsPdfReport(HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT && role != AdminRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        
        List<ExpressBookingAction> actions = expressBookingActionRepository.findAllByOrderByCreatedAtDesc();
        
        // Generate PDF using the existing PDF service pattern
        String html = generateExpressBookingsPdfHtml(actions);
        byte[] pdfBytes = convertHtmlToPdf(html);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "express_bookings_report.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/reports/cancellations/pdf")
    public ResponseEntity<byte[]> downloadCancellationsPdfReport(HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT && role != AdminRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        
        List<CancellationAction> actions = cancellationActionRepository.findAllByOrderByCreatedAtDesc();
        
        // Generate PDF using the existing PDF service pattern
        String html = generateCancellationsPdfHtml(actions);
        byte[] pdfBytes = convertHtmlToPdf(html);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cancellation_actions_report.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private String generateExpressBookingsPdfHtml(List<ExpressBookingAction> actions) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Express Bookings Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 10px; color: #333; line-height: 1.3; font-size: 10px; }");
        html.append(".report-container { width: 100%; background: white; padding: 0; }");
        html.append(".report-header { text-align: center; margin-bottom: 15px; border-bottom: 1px solid #2c3e50; padding-bottom: 10px; }");
        html.append(".report-title { font-size: 1.5rem; font-weight: bold; color: #2c3e50; margin: 0; }");
        html.append(".company-info { margin-top: 5px; color: #6c757d; font-size: 9px; }");
        html.append(".report-info { background: #f8f9fa; padding: 8px; border-radius: 3px; margin-bottom: 10px; font-size: 9px; }");
        html.append(".summary-grid { display: table; width: 100%; margin-bottom: 15px; }");
        html.append(".summary-card { display: table-cell; background: #fff; border: 1px solid #dee2e6; border-radius: 3px; padding: 8px; text-align: center; width: 33.33%; vertical-align: top; }");
        html.append(".summary-value { font-size: 14px; font-weight: bold; color: #2c3e50; margin: 3px 0; }");
        html.append(".summary-label { color: #6c757d; font-size: 8px; }");
        html.append(".highlight { background: #e8f5e8; border-color: #28a745; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 7px; page-break-inside: avoid; table-layout: fixed; }");
        html.append("th, td { border: 1px solid #ddd; padding: 2px 1px; text-align: left; vertical-align: top; word-wrap: break-word; overflow: hidden; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; color: #2c3e50; font-size: 6px; }");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        html.append("/* Column width constraints */");
        html.append("th:nth-child(1), td:nth-child(1) { width: 15%; }"); // Booking ID
        html.append("th:nth-child(2), td:nth-child(2) { width: 10%; }"); // Action
        html.append("th:nth-child(3), td:nth-child(3) { width: 15%; }"); // Performed By
        html.append("th:nth-child(4), td:nth-child(4) { width: 20%; }"); // Client Name
        html.append("th:nth-child(5), td:nth-child(5) { width: 18%; }"); // Client Email
        html.append("th:nth-child(6), td:nth-child(6) { width: 12%; }"); // Total Amount
        html.append("th:nth-child(7), td:nth-child(7) { width: 10%; }"); // Service Type
        html.append(".action-badge { padding: 1px 2px; border-radius: 1px; font-size: 5px; font-weight: bold; text-transform: uppercase; }");
        html.append(".action-created { background-color: #d4edda; color: #155724; }");
        html.append(".action-updated { background-color: #cce5ff; color: #004085; }");
        html.append(".action-cancelled { background-color: #f8d7da; color: #721c24; }");
        html.append(".footer { margin-top: 20px; text-align: center; color: #666; font-size: 8px; border-top: 1px solid #ddd; padding-top: 10px; }");
        html.append("</style></head><body>");
        
        html.append("<div class='report-container'>");
        html.append("<div class='report-header'>");
        html.append("<h1 class='report-title'>Express Bookings Report</h1>");
        html.append("<div class='company-info'>");
        html.append("<strong>AdBridge Lanka</strong><br>Digital Marketing Services<br>");
        html.append("Generated: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        html.append("</div></div>");
        
        html.append("<div class='report-info'>");
        html.append("<strong>Report Period:</strong> All Express Booking Actions<br>");
        html.append("<strong>Total Actions:</strong> ").append(actions.size()).append(" records");
        html.append("</div>");
        
        html.append("<div class='summary-grid'>");
        html.append("<div class='summary-card highlight'>");
        html.append("<div class='summary-label'>Total Actions</div>");
        html.append("<div class='summary-value'>").append(actions.size()).append("</div></div>");
        
        long createdCount = actions.stream().filter(a -> "CREATED".equals(a.getAction())).count();
        long updatedCount = actions.stream().filter(a -> "UPDATED".equals(a.getAction())).count();
        
        html.append("<div class='summary-card'>");
        html.append("<div class='summary-label'>Created Actions</div>");
        html.append("<div class='summary-value'>").append(createdCount).append("</div></div>");
        
        html.append("<div class='summary-card'>");
        html.append("<div class='summary-label'>Updated Actions</div>");
        html.append("<div class='summary-value'>").append(updatedCount).append("</div></div>");
        html.append("</div>");
        
        html.append("<h2>Express Booking Actions Details</h2>");
        html.append("<table><thead><tr>");
        html.append("<th>Booking ID</th><th>Action</th><th>Performed By</th><th>Client Name</th>");
        html.append("<th>Client Email</th><th>Total Amount (LKR)</th><th>Service Type</th><th>Created At</th>");
        html.append("</tr></thead><tbody>");
        
        for (ExpressBookingAction action : actions) {
            html.append("<tr>");
            html.append("<td>").append(action.getBookingId()).append("</td>");
            html.append("<td><span class='action-badge action-").append(action.getAction().toLowerCase()).append("'>").append(action.getAction()).append("</span></td>");
            html.append("<td>").append(action.getPerformedBy()).append("</td>");
            html.append("<td>").append(action.getClientName()).append("</td>");
            html.append("<td>").append(action.getClientEmail()).append("</td>");
            html.append("<td>").append(String.format("%.2f", action.getTotalAmount())).append("</td>");
            html.append("<td>").append(action.getServiceType()).append("</td>");
            html.append("<td>").append(action.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody></table>");
        html.append("<div class='footer'>");
        html.append("<p>Generated by AdBridgeLanka Support System</p>");
        html.append("<p>This report contains all express booking actions performed by support staff</p>");
        html.append("</div></div></body></html>");
        
        return html.toString();
    }

    private String generateCancellationsPdfHtml(List<CancellationAction> actions) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Cancellation Actions Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 10px; color: #333; line-height: 1.3; font-size: 10px; }");
        html.append(".report-container { width: 100%; background: white; padding: 0; }");
        html.append(".report-header { text-align: center; margin-bottom: 15px; border-bottom: 1px solid #2c3e50; padding-bottom: 10px; }");
        html.append(".report-title { font-size: 1.5rem; font-weight: bold; color: #2c3e50; margin: 0; }");
        html.append(".company-info { margin-top: 5px; color: #6c757d; font-size: 9px; }");
        html.append(".report-info { background: #f8f9fa; padding: 8px; border-radius: 3px; margin-bottom: 10px; font-size: 9px; }");
        html.append(".summary-grid { display: table; width: 100%; margin-bottom: 15px; }");
        html.append(".summary-card { display: table-cell; background: #fff; border: 1px solid #dee2e6; border-radius: 3px; padding: 8px; text-align: center; width: 33.33%; vertical-align: top; }");
        html.append(".summary-value { font-size: 14px; font-weight: bold; color: #2c3e50; margin: 3px 0; }");
        html.append(".summary-label { color: #6c757d; font-size: 8px; }");
        html.append(".highlight { background: #e8f5e8; border-color: #28a745; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 7px; page-break-inside: avoid; table-layout: fixed; }");
        html.append("th, td { border: 1px solid #ddd; padding: 2px 1px; text-align: left; vertical-align: top; word-wrap: break-word; overflow: hidden; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; color: #2c3e50; font-size: 6px; }");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        html.append("/* Column width constraints */");
        html.append("th:nth-child(1), td:nth-child(1) { width: 12%; }"); // Booking ID
        html.append("th:nth-child(2), td:nth-child(2) { width: 8%; }"); // Action
        html.append("th:nth-child(3), td:nth-child(3) { width: 12%; }"); // Performed By
        html.append("th:nth-child(4), td:nth-child(4) { width: 15%; }"); // Client Name
        html.append("th:nth-child(5), td:nth-child(5) { width: 15%; }"); // Client Email
        html.append("th:nth-child(6), td:nth-child(6) { width: 10%; }"); // Total Amount
        html.append("th:nth-child(7), td:nth-child(7) { width: 8%; }"); // Service Type
        html.append("th:nth-child(8), td:nth-child(8) { width: 20%; }"); // Cancellation Reason
        html.append(".action-badge { padding: 1px 2px; border-radius: 1px; font-size: 5px; font-weight: bold; text-transform: uppercase; }");
        html.append(".action-cancelled { background-color: #f8d7da; color: #721c24; }");
        html.append(".action-refunded { background-color: #cce5ff; color: #004085; }");
        html.append(".action-processed { background-color: #d4edda; color: #155724; }");
        html.append(".reason-cell { max-width: 100px; word-wrap: break-word; }");
        html.append(".footer { margin-top: 20px; text-align: center; color: #666; font-size: 8px; border-top: 1px solid #ddd; padding-top: 10px; }");
        html.append("</style></head><body>");
        
        html.append("<div class='report-container'>");
        html.append("<div class='report-header'>");
        html.append("<h1 class='report-title'>Cancellation Actions Report</h1>");
        html.append("<div class='company-info'>");
        html.append("<strong>AdBridge Lanka</strong><br>Digital Marketing Services<br>");
        html.append("Generated: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        html.append("</div></div>");
        
        html.append("<div class='report-info'>");
        html.append("<strong>Report Period:</strong> All Cancellation Actions<br>");
        html.append("<strong>Total Actions:</strong> ").append(actions.size()).append(" records");
        html.append("</div>");
        
        html.append("<div class='summary-grid'>");
        html.append("<div class='summary-card highlight'>");
        html.append("<div class='summary-label'>Total Actions</div>");
        html.append("<div class='summary-value'>").append(actions.size()).append("</div></div>");
        
        long cancelledCount = actions.stream().filter(a -> "CANCELLED".equals(a.getAction())).count();
        long refundedCount = actions.stream().filter(a -> "REFUNDED".equals(a.getAction())).count();
        
        html.append("<div class='summary-card'>");
        html.append("<div class='summary-label'>Cancelled Bookings</div>");
        html.append("<div class='summary-value'>").append(cancelledCount).append("</div></div>");
        
        html.append("<div class='summary-card'>");
        html.append("<div class='summary-label'>Refunded Bookings</div>");
        html.append("<div class='summary-value'>").append(refundedCount).append("</div></div>");
        html.append("</div>");
        
        html.append("<h2>Cancellation Actions Details</h2>");
        html.append("<table><thead><tr>");
        html.append("<th>Booking ID</th><th>Action</th><th>Performed By</th><th>Client Name</th>");
        html.append("<th>Client Email</th><th>Total Amount (LKR)</th><th>Service Type</th><th>Cancellation Reason</th><th>Created At</th>");
        html.append("</tr></thead><tbody>");
        
        for (CancellationAction action : actions) {
            html.append("<tr>");
            html.append("<td>").append(action.getBookingId()).append("</td>");
            html.append("<td><span class='action-badge action-").append(action.getAction().toLowerCase()).append("'>").append(action.getAction()).append("</span></td>");
            html.append("<td>").append(action.getPerformedBy()).append("</td>");
            html.append("<td>").append(action.getClientName()).append("</td>");
            html.append("<td>").append(action.getClientEmail()).append("</td>");
            html.append("<td>").append(String.format("%.2f", action.getTotalAmount())).append("</td>");
            html.append("<td>").append(action.getServiceType()).append("</td>");
            html.append("<td class='reason-cell'>").append(action.getCancellationReason()).append("</td>");
            html.append("<td>").append(action.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody></table>");
        html.append("<div class='footer'>");
        html.append("<p>Generated by AdBridgeLanka Support System</p>");
        html.append("<p>This report contains all cancellation actions performed by support staff</p>");
        html.append("</div></div></body></html>");
        
        return html.toString();
    }

    private byte[] convertHtmlToPdf(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Use the same approach as existing PDF services in the system
            com.itextpdf.html2pdf.ConverterProperties properties = new com.itextpdf.html2pdf.ConverterProperties();
            properties.setCharset("UTF-8");
            
            // Convert HTML to PDF with proper settings
            com.itextpdf.html2pdf.HtmlConverter.convertToPdf(html, outputStream, properties);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert HTML to PDF", e);
        }
    }
}


