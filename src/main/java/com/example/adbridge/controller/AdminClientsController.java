package com.example.adbridge.controller;

import com.example.adbridge.model.User;
import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Payment;
import com.example.adbridge.model.CommunicationHistory;
import com.example.adbridge.model.AdminRole;
import com.example.adbridge.model.Contact;
import com.example.adbridge.model.Notification;
import com.example.adbridge.repo.UserRepository;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.PaymentRepository;
import com.example.adbridge.repo.SupportRequestRepository;
import com.example.adbridge.repo.CommunicationHistoryRepository;
import com.example.adbridge.repo.ContactRepository;
import com.example.adbridge.repo.NotificationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/clients")
public class AdminClientsController {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SupportRequestRepository supportRepository;
    private final CommunicationHistoryRepository communicationRepository;
    private final ContactRepository contactRepository;
    private final NotificationRepository notificationRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminClientsController(UserRepository userRepository,
                                  BookingRepository bookingRepository,
                                  PaymentRepository paymentRepository,
                                  SupportRequestRepository supportRepository,
                                  CommunicationHistoryRepository communicationRepository,
                                  ContactRepository contactRepository,
                                  NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.supportRepository = supportRepository;
        this.communicationRepository = communicationRepository;
        this.contactRepository = contactRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                      @RequestParam(value = "status", required = false) User.ClientStatus status,
                      @RequestParam(value = "sortBy", required = false) String sortBy,
                      @RequestParam(value = "hasUnpaid", required = false) Boolean hasUnpaid,
                      @RequestParam(value = "hasTickets", required = false) Boolean hasTickets,
                      Model model, HttpSession session, HttpServletResponse response) {
        
        // Add no-cache headers to prevent browser caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        List<User> clients = userRepository.findAll();
        
        // Apply search filter
        if (q != null && !q.trim().isEmpty()) {
            String qq = q.trim().toLowerCase();
            clients = clients.stream().filter(u ->
                    (u.getFullName() != null && u.getFullName().toLowerCase().contains(qq)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(qq)) ||
                    (u.getPhone() != null && u.getPhone().toLowerCase().contains(qq)) ||
                    (u.getCompanyName() != null && u.getCompanyName().toLowerCase().contains(qq))
            ).toList();
        }
        
        // Apply status filter
        if (status != null) {
            clients = clients.stream().filter(u -> u.getStatus() == status).toList();
        }
        
        // Apply unpaid filter
        if (hasUnpaid != null && hasUnpaid) {
            clients = clients.stream().filter(u -> {
                List<Booking> bookings = bookingRepository.findByEmailOrderByIdDesc(u.getEmail());
                return bookings.stream().anyMatch(b -> "PENDING".equals(b.getPaymentStatus()));
            }).toList();
        }
        
        // Apply tickets filter
        if (hasTickets != null && hasTickets) {
            clients = clients.stream().filter(u -> {
                long ticketCount = supportRepository.findAll().stream()
                        .filter(t -> u.getEmail().equalsIgnoreCase(t.getClientEmail()))
                        .count();
                return ticketCount > 0;
            }).toList();
        }
        
        // Apply approval status sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            switch (sortBy.toLowerCase()) {
                case "approval_pending":
                    clients = clients.stream()
                            .filter(c -> c.getApprovalStatus() == User.ApprovalStatus.PENDING)
                            .toList();
                    break;
                case "approval_approved":
                    clients = clients.stream()
                            .filter(c -> c.getApprovalStatus() == User.ApprovalStatus.APPROVED)
                            .toList();
                    break;
                case "approval_rejected":
                    clients = clients.stream()
                            .filter(c -> c.getApprovalStatus() == User.ApprovalStatus.REJECTED)
                            .toList();
                    break;
            }
        }
        
        model.addAttribute("title", "Clients · Admin");
        model.addAttribute("pageHeading", "Clients");
        model.addAttribute("activeMenu", "clients");
        model.addAttribute("clients", clients);
        model.addAttribute("q", q);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentHasUnpaid", hasUnpaid);
        model.addAttribute("currentHasTickets", hasTickets);
        model.addAttribute("statusOptions", User.ClientStatus.values());
        
        // Add contact messages for SUPPORT role and SUPER_ADMIN (only CREATE_NEW_CLIENT requests)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.SUPPORT || role == AdminRole.SUPER_ADMIN) {
            List<Contact> contacts = contactRepository.findByIsRepliedFalseOrderByCreatedAtDesc().stream()
                .filter(c -> "CREATE_NEW_CLIENT".equals(c.getRequestType()))
                .toList();
            System.out.println("=== CONTACT DEBUG ===");
            System.out.println("Role: " + role);
            System.out.println("Total contacts found: " + contacts.size());
            for (Contact c : contacts) {
                System.out.println("Contact ID: " + c.getId() + ", Name: " + c.getName() + ", Email: " + c.getEmail() + ", Subject: " + c.getSubject() + ", IsReplied: " + c.getIsReplied());
            }
            model.addAttribute("contacts", contacts);
        }
        
        return "admin/clients/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        Optional<User> u = userRepository.findById(id);
        if (u.isEmpty()) return "redirect:/admin/clients";
        User user = u.get();
        List<Booking> bookings = bookingRepository.findByEmailOrderByIdDesc(user.getEmail());
        long paymentCount = bookings.stream()
                .map(b -> paymentRepository.findAllByBookingId(b.getBookingId()))
                .mapToLong(List::size).sum();
        long ticketCount = supportRepository.findAll().stream()
                .filter(t -> user.getEmail().equalsIgnoreCase(t.getClientEmail()))
                .count();
        
        // Get communication history
        List<CommunicationHistory> communications = communicationRepository.findByUserOrderByCreatedAtDesc(user);
        
        model.addAttribute("title", "Client · Admin");
        model.addAttribute("pageHeading", "Client Profile");
        model.addAttribute("activeMenu", "clients");
        model.addAttribute("client", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("paymentCount", paymentCount);
        model.addAttribute("ticketCount", ticketCount);
        model.addAttribute("communications", communications);
        model.addAttribute("statusOptions", User.ClientStatus.values());
        model.addAttribute("communicationTypes", CommunicationHistory.CommunicationType.values());
        return "admin/clients/detail";
    }

    @GetMapping("/new")
    public String newClient(Model model, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT) {
            return "redirect:/admin/clients";
        }
        
        model.addAttribute("title", "New Client · Admin");
        model.addAttribute("pageHeading", "Add New Client");
        model.addAttribute("activeMenu", "clients");
        model.addAttribute("client", new User());
        model.addAttribute("statusOptions", User.ClientStatus.values());
        return "admin/clients/form";
    }

    @PostMapping
    public String createClient(@ModelAttribute User client, 
                              @RequestParam(value = "contactId", required = false) Long contactId,
                              RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT) {
            ra.addFlashAttribute("error", "Access denied. Only Support Officers can create new clients.");
            return "redirect:/admin/clients";
        }
        
        try {
            // Set default status if not provided
            if (client.getStatus() == null) {
                client.setStatus(User.ClientStatus.ACTIVE);
            }
            
            // Set approval status based on creation method
            if (contactId != null) {
                // Created from contact message by Support Officer - needs approval
                client.setApprovalStatus(User.ApprovalStatus.PENDING);
            } else {
                // Created manually - auto-approved
                client.setApprovalStatus(User.ApprovalStatus.APPROVED);
            }
            
            // Generate userId if not provided
            if (client.getUserId() == null || client.getUserId().isEmpty()) {
                client.setUserId("USR" + (System.currentTimeMillis() % 100000));
            }
            
            // Generate username and password if not provided
            if (client.getUsername() == null || client.getUsername().isEmpty()) {
                String baseUsername = client.getFullName().toLowerCase().replaceAll("[^a-z0-9]", "");
                // Limit base username to 8 characters to leave room for timestamp
                if (baseUsername.length() > 8) {
                    baseUsername = baseUsername.substring(0, 8);
                }
                String username = baseUsername + (System.currentTimeMillis() % 100000);
                client.setUsername(username);
            } else {
                // Clean existing username to remove any duplicates
                String cleanUsername = client.getUsername().trim();
                if (cleanUsername.contains(",")) {
                    cleanUsername = cleanUsername.split(",")[0].trim();
                }
                client.setUsername(cleanUsername);
            }
            
            String originalPassword = null;
            if (client.getPassword() == null || client.getPassword().isEmpty()) {
                String password = "client" + (System.currentTimeMillis() % 10000);
                originalPassword = password; // Store original for notification
                client.setPassword(passwordEncoder.encode(password));
            } else {
                // Clean existing password to remove any duplicates
                String cleanPassword = client.getPassword().trim();
                if (cleanPassword.contains(",")) {
                    cleanPassword = cleanPassword.split(",")[0].trim();
                }
                // Only encode if it's not already encoded (doesn't start with $2a$)
                if (!cleanPassword.startsWith("$2a$")) {
                    originalPassword = cleanPassword; // Store original for notification
                    client.setPassword(passwordEncoder.encode(cleanPassword));
                } else {
                    client.setPassword(cleanPassword);
                }
            }
            
            // Check for existing username (only username needs to be unique)
            if (userRepository.existsByUsername(client.getUsername())) {
                ra.addFlashAttribute("error", "A client with this username already exists: " + client.getUsername());
                if (contactId != null) {
                    return "redirect:/admin/clients/contact/" + contactId + "/create-client";
                }
                return "redirect:/admin/clients/new";
            }
            
            // If created from contact, link to contact before saving
            if (contactId != null) {
                Optional<Contact> contactOpt = contactRepository.findById(contactId);
                if (contactOpt.isPresent()) {
                    Contact contact = contactOpt.get();
                    client.setCreatedFromContact(contact); // Link the client to the contact
                    System.out.println("DEBUG: Linking client to contact ID: " + contactId);
                    System.out.println("DEBUG: Contact requestedByUser: " + (contact.getRequestedByUser() != null));
                    if (contact.getRequestedByUser() != null) {
                        System.out.println("DEBUG: Requesting user: " + contact.getRequestedByUser().getUsername());
                    }
                }
            }
            
            userRepository.save(client);
            
            // If created from contact, mark contact as replied and ensure relationship is saved
            if (contactId != null) {
                Optional<Contact> contactOpt = contactRepository.findById(contactId);
                if (contactOpt.isPresent()) {
                    Contact contact = contactOpt.get();
                    contact.setIsReplied(true);
                    contactRepository.save(contact);
                    
                    // Force save the user again to ensure relationship is persisted
                    client.setCreatedFromContact(contact);
                    userRepository.save(client);
                    System.out.println("DEBUG: Saved client with contact relationship");
                }
            }
            
            ra.addFlashAttribute("success", "Client created successfully");
            return "redirect:/admin/clients";
        } catch (Exception e) {
            e.printStackTrace(); // Print full stack trace for debugging
            ra.addFlashAttribute("error", "Error creating client: " + e.getMessage());
            if (contactId != null) {
                return "redirect:/admin/clients/contact/" + contactId + "/create-client";
            }
            return "redirect:/admin/clients/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editClient(@PathVariable String id, Model model) {
        Optional<User> u = userRepository.findById(id);
        if (u.isEmpty()) return "redirect:/admin/clients";
        
        model.addAttribute("title", "Edit Client · Admin");
        model.addAttribute("pageHeading", "Edit Client");
        model.addAttribute("activeMenu", "clients");
        model.addAttribute("client", u.get());
        model.addAttribute("statusOptions", User.ClientStatus.values());
        return "admin/clients/form";
    }

    @PostMapping("/{id}")
    public String updateClient(@PathVariable String id, @ModelAttribute User updatedClient, RedirectAttributes ra) {
        try {
            Optional<User> existingUser = userRepository.findById(id);
            if (existingUser.isEmpty()) {
                ra.addFlashAttribute("error", "Client not found");
                return "redirect:/admin/clients";
            }
            
            User user = existingUser.get();
            user.setFullName(updatedClient.getFullName());
            user.setEmail(updatedClient.getEmail());
            user.setPhone(updatedClient.getPhone());
            user.setCompanyName(updatedClient.getCompanyName());
            user.setStatus(updatedClient.getStatus());
            user.setInternalNotes(updatedClient.getInternalNotes());
            
            userRepository.save(user);
            ra.addFlashAttribute("success", "Client updated successfully");
            return "redirect:/admin/clients/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating client: " + e.getMessage());
            return "redirect:/admin/clients/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable String id, RedirectAttributes ra) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                ra.addFlashAttribute("success", "Client deleted successfully");
            } else {
                ra.addFlashAttribute("error", "Client not found");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting client: " + e.getMessage());
        }
        return "redirect:/admin/clients";
    }

    @PostMapping("/{id}/communication")
    public String addCommunication(@PathVariable String id, 
                                 @RequestParam String type,
                                 @RequestParam String subject,
                                 @RequestParam String description,
                                 @RequestParam String staffMember,
                                 RedirectAttributes ra) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                ra.addFlashAttribute("error", "Client not found");
                return "redirect:/admin/clients";
            }
            
            CommunicationHistory.CommunicationType commType = CommunicationHistory.CommunicationType.valueOf(type);
            CommunicationHistory communication = new CommunicationHistory(
                user.get(), commType, subject, description, staffMember
            );
            communicationRepository.save(communication);
            
            ra.addFlashAttribute("success", "Communication added successfully");
            return "redirect:/admin/clients/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error adding communication: " + e.getMessage());
            return "redirect:/admin/clients/" + id;
        }
    }

    @PostMapping("/contact/{contactId}/create-client")
    public String createClientFromContact(@PathVariable Long contactId, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT) {
            ra.addFlashAttribute("error", "Access denied. Only Support Officers can create clients from contacts.");
            return "redirect:/admin/clients";
        }

        try {
            Optional<Contact> contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isEmpty()) {
                ra.addFlashAttribute("error", "Contact message not found");
                return "redirect:/admin/clients";
            }
            
            Contact contact = contactOpt.get();
            
            // Create new client from contact information
            User newClient = new User();
            newClient.setFullName(contact.getName());
            newClient.setEmail(contact.getEmail());
            newClient.setPhone(contact.getPhone());
            newClient.setCompanyName(contact.getSubject().replace("Contact from ", ""));
            newClient.setStatus(User.ClientStatus.ACTIVE);
            newClient.setInternalNotes("Created from contact message: " + contact.getMessage());
            newClient.setCreatedFromContact(contact); // Link to the contact request
            
            userRepository.save(newClient);
            
            // Mark contact as replied
            contact.setIsReplied(true);
            contactRepository.save(contact);
            
            ra.addFlashAttribute("success", "Client created successfully from contact message");
            return "redirect:/admin/clients/" + newClient.getUserId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error creating client: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }

    @GetMapping("/contact/{contactId}")
    public String viewContactMessage(@PathVariable Long contactId, Model model, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT) {
            return "redirect:/admin/clients";
        }

        Optional<Contact> contactOpt = contactRepository.findById(contactId);
        if (contactOpt.isEmpty()) {
            return "redirect:/admin/clients";
        }
        
        Contact contact = contactOpt.get();
        
        // Mark as read
        if (!contact.getIsRead()) {
            contact.setIsRead(true);
            contactRepository.save(contact);
        }
        
        model.addAttribute("title", "Contact Message · Admin");
        model.addAttribute("pageHeading", "Contact Message");
        model.addAttribute("activeMenu", "clients");
        model.addAttribute("contact", contact);
        return "admin/clients/contact-detail";
    }

    @GetMapping("/contact/{contactId}/create-client")
    public String createClientFromContact(@PathVariable Long contactId, Model model, HttpSession session) {
        try {
            AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
            if (role != AdminRole.SUPPORT) {
                return "redirect:/admin/clients";
            }

            Optional<Contact> contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isEmpty()) {
                return "redirect:/admin/clients";
            }
            
            Contact contact = contactOpt.get();
            
        // Pre-populate client data from contact
        User newClient = new User();
        // Ensure userId is null for new client creation
        newClient.setUserId(null);
        newClient.setFullName(contact.getName());
        newClient.setEmail(contact.getEmail());
        newClient.setPhone(contact.getPhone());
        newClient.setCompanyName(contact.getSubject().replace("Contact from ", ""));
        newClient.setStatus(User.ClientStatus.ACTIVE);
        newClient.setInternalNotes("Created from contact message: " + contact.getMessage());
        newClient.setCreatedFromContact(contact); // Link to the contact request
        
        // Generate simple username and password
        String baseUsername = contact.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
        // Limit base username to 8 characters to leave room for timestamp
        if (baseUsername.length() > 8) {
            baseUsername = baseUsername.substring(0, 8);
        }
        String username = baseUsername + (System.currentTimeMillis() % 100000); // Use shorter timestamp for uniqueness
        String password = "client" + (System.currentTimeMillis() % 10000); // Simple password with 4 digits
        
        // Store original password for notification display
        String originalPassword = password.trim();
        
        // Ensure no duplicates by cleaning the values and encode password
        newClient.setUsername(username.trim());
        newClient.setPassword(passwordEncoder.encode(password.trim()));
            
            model.addAttribute("title", "Create Client from Contact · Admin");
            model.addAttribute("pageHeading", "Create New Client");
            model.addAttribute("activeMenu", "clients");
            model.addAttribute("client", newClient);
            model.addAttribute("originalPassword", originalPassword);
            model.addAttribute("statusOptions", User.ClientStatus.values());
            model.addAttribute("contactId", contactId);
            model.addAttribute("isFromContact", true);
            return "admin/clients/form";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/clients";
        }
    }

    @PostMapping("/contact/{contactId}/mark-replied")
    public String markContactReplied(@PathVariable Long contactId, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPPORT) {
            ra.addFlashAttribute("error", "Access denied. Only Support Officers can manage contacts.");
            return "redirect:/admin/clients";
        }

        try {
            Optional<Contact> contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isEmpty()) {
                ra.addFlashAttribute("error", "Contact message not found");
                return "redirect:/admin/clients";
            }
            
            Contact contact = contactOpt.get();
            contact.setIsReplied(true);
            contactRepository.save(contact);
            
            ra.addFlashAttribute("success", "Contact marked as replied");
            return "redirect:/admin/clients";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating contact: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }

    @PostMapping("/{userId}/approve")
    public String approveClient(@PathVariable String userId, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR) {
            ra.addFlashAttribute("error", "Access denied. Only Directors can approve clients.");
            return "redirect:/admin/clients";
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                ra.addFlashAttribute("error", "Client not found");
                return "redirect:/admin/clients";
            }

            User user = userOpt.get();
            
            // Clean username and password to remove any duplicates
            String originalPasswordForNotification = null;
            if (user.getUsername() != null && user.getUsername().contains(",")) {
                String cleanUsername = user.getUsername().split(",")[0].trim();
                user.setUsername(cleanUsername);
            }
            if (user.getPassword() != null && user.getPassword().contains(",")) {
                String cleanPassword = user.getPassword().split(",")[0].trim();
                originalPasswordForNotification = cleanPassword; // Store original for notification
                // Only encode if it's not already encoded (doesn't start with $2a$)
                if (!cleanPassword.startsWith("$2a$")) {
                    user.setPassword(passwordEncoder.encode(cleanPassword));
                } else {
                    user.setPassword(cleanPassword);
                }
            } else if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                // If password is not encoded and doesn't contain commas, encode it
                originalPasswordForNotification = user.getPassword(); // Store original for notification
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            
            user.setApprovalStatus(User.ApprovalStatus.APPROVED);
            User savedUser = userRepository.save(user);
            userRepository.flush(); // Force immediate database write
            
            // Verify the change was actually persisted
            User verifyUser = userRepository.findById(userId).orElse(null);
            
            System.out.println("=== APPROVE DEBUG ===");
            System.out.println("DEBUG: User ID: " + userId);
            System.out.println("DEBUG: User Name: " + user.getFullName());
            System.out.println("DEBUG: User Email: " + user.getEmail());
            System.out.println("DEBUG: Status before save: APPROVED");
            System.out.println("DEBUG: Status after save: " + savedUser.getApprovalStatus());
            System.out.println("DEBUG: Status after verification: " + (verifyUser != null ? verifyUser.getApprovalStatus() : "NULL"));
            System.out.println("DEBUG: Verification User Name: " + (verifyUser != null ? verifyUser.getFullName() : "NULL"));
            System.out.println("DEBUG: Verification User Email: " + (verifyUser != null ? verifyUser.getEmail() : "NULL"));
            System.out.println("=== END APPROVE DEBUG ===");
            
            System.out.println("DEBUG: Approving user: " + user.getUsername());
            System.out.println("DEBUG: User createdFromContact: " + (user.getCreatedFromContact() != null));
            if (user.getCreatedFromContact() != null) {
                System.out.println("DEBUG: Contact ID: " + user.getCreatedFromContact().getId());
                System.out.println("DEBUG: Contact requestedByUser: " + (user.getCreatedFromContact().getRequestedByUser() != null));
            }
            
            // Create notification for the user who requested this client
            if (user.getCreatedFromContact() != null && user.getCreatedFromContact().getRequestedByUser() != null) {
                User requestingUser = user.getCreatedFromContact().getRequestedByUser();
                
                // Use the original password for display if available, otherwise generate a new one
                String displayPassword;
                if (originalPasswordForNotification != null) {
                    displayPassword = originalPasswordForNotification;
                } else {
                    // Generate a new password for the client
                    String newPassword = "client" + (System.currentTimeMillis() % 10000);
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    displayPassword = newPassword;
                }
                
                String notificationMessage = String.format(
                    "Your requested client has been approved! Client Details: Username: %s, Password: %s, User ID: %s",
                    user.getUsername(),
                    displayPassword,
                    user.getUserId()
                );
                
                Notification notification = new Notification(
                    requestingUser,
                    "Client Approved",
                    notificationMessage,
                    Notification.NotificationType.SUCCESS
                );
                notificationRepository.save(notification);
                
                System.out.println("DEBUG: Created notification for user: " + requestingUser.getUsername());
                System.out.println("DEBUG: Notification message: " + notificationMessage);
            } else {
                System.out.println("DEBUG: No notification created - createdFromContact: " + (user.getCreatedFromContact() != null));
                if (user.getCreatedFromContact() != null) {
                    System.out.println("DEBUG: requestedByUser: " + (user.getCreatedFromContact().getRequestedByUser() != null));
                }
            }

            ra.addFlashAttribute("success", "Client approved successfully");
            return "redirect:/admin/clients?t=" + System.currentTimeMillis();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error approving client: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }

    @PostMapping("/{userId}/reject")
    public String rejectClient(@PathVariable String userId, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR) {
            ra.addFlashAttribute("error", "Access denied. Only Directors can reject clients.");
            return "redirect:/admin/clients";
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                ra.addFlashAttribute("error", "Client not found");
                return "redirect:/admin/clients";
            }

            User user = userOpt.get();
            user.setApprovalStatus(User.ApprovalStatus.REJECTED);
            User savedUser = userRepository.save(user);
            userRepository.flush(); // Force immediate database write
            
            // Verify the change was actually persisted
            User verifyUser = userRepository.findById(userId).orElse(null);
            
            System.out.println("DEBUG: Client rejected - User ID: " + userId);
            System.out.println("DEBUG: Status before save: REJECTED");
            System.out.println("DEBUG: Status after save: " + savedUser.getApprovalStatus());
            System.out.println("DEBUG: Status after verification: " + (verifyUser != null ? verifyUser.getApprovalStatus() : "NULL"));
            System.out.println("DEBUG: User saved successfully");

            ra.addFlashAttribute("success", "Client rejected");
            return "redirect:/admin/clients?t=" + System.currentTimeMillis();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error rejecting client: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }

    @PostMapping("/test-set-pending")
    public String setAllClientsPending(RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.DIRECTOR) {
            ra.addFlashAttribute("error", "Access denied. Only Directors can set clients to pending.");
            return "redirect:/admin/clients";
        }

        try {
            List<User> allClients = userRepository.findAll();
            for (User client : allClients) {
                client.setApprovalStatus(User.ApprovalStatus.PENDING);
                userRepository.save(client);
            }

            ra.addFlashAttribute("success", "All clients set to PENDING status for testing");
            return "redirect:/admin/clients";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error setting clients to pending: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }

    @PostMapping("/fix-passwords")
    public String fixAllPasswords(RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Super Admin can fix passwords.");
            return "redirect:/admin/clients";
        }

        try {
            List<User> allUsers = userRepository.findAll();
            int fixedCount = 0;
            
            for (User user : allUsers) {
                String password = user.getPassword();
                // If password doesn't start with $2a$ (BCrypt), encode it
                if (password != null && !password.startsWith("$2a$")) {
                    // Clean password if it has commas
                    if (password.contains(",")) {
                        password = password.split(",")[0].trim();
                    }
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    fixedCount++;
                    System.out.println("DEBUG: Fixed password for user: " + user.getUsername());
                }
            }
            
            ra.addFlashAttribute("success", "Fixed " + fixedCount + " user passwords. All passwords are now properly encoded.");
            return "redirect:/admin/clients";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error fixing passwords: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }

}






