package com.example.adbridge.controller;

import com.example.adbridge.model.Contact;
import com.example.adbridge.model.Notification;
import com.example.adbridge.model.User;
import com.example.adbridge.repo.ContactRepository;
import com.example.adbridge.repo.NotificationRepository;
import com.example.adbridge.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/contact")
    public String contactPage(Model model, HttpSession session) {
        model.addAttribute("title", "Contact Us");
        model.addAttribute("showBookInNav", true);
        model.addAttribute("showPaymentInNav", false);
        
        // Add user to model for auto-filling form if logged in
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
        
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam String companyName,
                               @RequestParam String contactPerson,
                               @RequestParam String email,
                               @RequestParam String phone,
                               @RequestParam String message,
                               @RequestParam(required = false) String requestType,
                               @RequestParam(required = false) String bookingId,
                               @RequestParam(required = false) String clientName,
                               @RequestParam(required = false) String clientEmail,
                               @RequestParam(required = false) String clientCompany,
                               @RequestParam(required = false) String clientPhone,
                               @RequestParam(required = false) String requestReason,
                               RedirectAttributes ra,
                               HttpSession session) {
        System.out.println("=== CONTACT POST RECEIVED ===");
        System.out.println("companyName: " + companyName);
        System.out.println("contactPerson: " + contactPerson);
        System.out.println("email: " + email);
        System.out.println("phone: " + phone);
        System.out.println("message: " + message);
        System.out.println("requestType: " + requestType);
        System.out.println("bookingId: " + bookingId);
        System.out.println("clientName: " + clientName);
        System.out.println("clientEmail: " + clientEmail);
        System.out.println("clientCompany: " + clientCompany);
        System.out.println("clientPhone: " + clientPhone);
        System.out.println("requestReason: " + requestReason);
        try {
            Contact contact = new Contact();
            contact.setName(contactPerson);
            contact.setEmail(email);
            contact.setPhone(phone);
            contact.setSubject("Contact from " + companyName);
            contact.setMessage(message);
            
            // Set new structured fields
            contact.setRequestType(requestType);
            contact.setBookingId(bookingId);
            contact.setClientName(clientName);
            contact.setClientEmail(clientEmail);
            contact.setClientCompany(clientCompany);
            contact.setClientPhone(clientPhone);
            contact.setRequestReason(requestReason);
            
            // Link the contact to the user who made the request (if logged in)
            User user = (User) session.getAttribute("user");
            if (user != null) {
                contact.setRequestedByUser(user);
            }
            
            contactRepository.save(contact);

            // Debug: Print contact details
            System.out.println("=== CONTACT SAVED ===");
            System.out.println("Contact ID: " + contact.getId());
            System.out.println("Name: " + contact.getName());
            System.out.println("Email: " + contact.getEmail());
            System.out.println("Subject: " + contact.getSubject());
            System.out.println("Message: " + contact.getMessage());
            System.out.println("IsReplied: " + contact.getIsReplied());
            System.out.println("Created At: " + contact.getCreatedAt());

            // Check if user is logged in and send notification
            if (user != null) {
                // Create notification for the user
                Notification notification = new Notification(
                    user,
                    "Contact Message Sent",
                    "Your contact message has been sent successfully. Our support team will review it and get back to you soon.",
                    Notification.NotificationType.INFO
                );
                notificationRepository.save(notification);
            }
            
            ra.addFlashAttribute("success", "Thank you for your message! We'll get back to you soon.");
            return "redirect:/contact";
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Sorry, there was an error sending your message. Please try again.");
            return "redirect:/contact";
        }
    }
}

