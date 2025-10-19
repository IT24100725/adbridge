package com.example.adbridge.controller;

import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Payment;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.PaymentRepository;
import com.example.adbridge.service.VersionHistoryService;
import com.example.adbridge.service.PricingService;
import com.example.adbridge.service.PaymentIdService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Controller
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final VersionHistoryService versionHistoryService;
    private final PricingService pricingService;
    private final PaymentIdService paymentIdService;

    public PaymentController(BookingRepository bookingRepository, PaymentRepository paymentRepository, VersionHistoryService versionHistoryService, PricingService pricingService, PaymentIdService paymentIdService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.versionHistoryService = versionHistoryService;
        this.pricingService = pricingService;
        this.paymentIdService = paymentIdService;
    }

    @GetMapping("/payment")
    public String myPayments(Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/payment";
        }

        // Get user's payments
        List<Payment> userPayments = paymentRepository.findByUserEmail(((com.example.adbridge.model.User) currentUser).getEmail());

        model.addAttribute("title", "My Payments");
        model.addAttribute("showBookInNav", true);
        model.addAttribute("showPaymentInNav", true);
        model.addAttribute("user", currentUser);
        model.addAttribute("payments", userPayments);

        return "my-payments";
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/my-bookings";
        }

        // Get user's bookings
        List<Booking> userBookings = bookingRepository.findByEmailOrderByIdDesc(((com.example.adbridge.model.User) currentUser).getEmail());

        // Add cancellation eligibility for each booking
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (Booking booking : userBookings) {
            boolean canCancel = false;
            if (booking.getPaymentStatus() != null && !booking.getPaymentStatus().equals("CANCELLED")) {
                if (booking.getCampaignStatus() != null && booking.getCampaignStatus().equals("APPROVED")) {
                    if (booking.getLastModifiedAt() != null) {
                        // Check if less than 2 days have passed since approval
                        java.time.LocalDateTime twoDaysAgo = now.minusDays(2);
                        canCancel = booking.getLastModifiedAt().isAfter(twoDaysAgo);
                    } else {
                        canCancel = true; // If no lastModifiedAt, allow cancellation
                    }
                } else {
                    canCancel = true; // If not approved yet, allow cancellation
                }
            }
            // Add a custom property to track cancellation eligibility
            // We'll use a simple approach by checking in the template
        }

        model.addAttribute("title", "My Bookings");
        model.addAttribute("showBookInNav", true);
        model.addAttribute("showPaymentInNav", false);
        model.addAttribute("user", currentUser);
        model.addAttribute("bookings", userBookings);
        model.addAttribute("currentTime", now);

        return "my-bookings";
    }

    @GetMapping("/cancel-booking/{bookingId}")
    public String cancelBooking(@PathVariable String bookingId, Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/my-bookings";
        }

        // Find the booking
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            model.addAttribute("error", "Booking not found");
            return "redirect:/my-bookings";
        }

        Booking booking = bookingOpt.get();

        // Check if the booking belongs to the current user
        if (!booking.getEmail().equals(((com.example.adbridge.model.User) currentUser).getEmail())) {
            model.addAttribute("error", "You can only cancel your own bookings");
            return "redirect:/my-bookings";
        }

        // Check if the campaign is already approved (cannot cancel approved campaigns)
        if ("APPROVED".equals(booking.getCampaignStatus()) || "APPROVED".equals(booking.getPaymentStatus())) {
            model.addAttribute("error", "Cannot cancel approved campaigns. Please contact support if needed.");
            return "redirect:/my-bookings";
        }

        // Update booking status to cancelled
        booking.setPaymentStatus("CANCELLED");
        booking.setCampaignStatus("CANCELLED");
        booking.setLastModifiedAt(java.time.LocalDateTime.now());
        booking.setIsEdited(true);

        bookingRepository.save(booking);

        // Also update associated payment if it exists
        List<Payment> payments = paymentRepository.findAllByBookingId(bookingId);
        payments.stream()
            .filter(payment -> payment.getDeleted() == null || !payment.getDeleted())
            .forEach(payment -> {
                payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
                payment.setLastAction("CANCELLED");
                payment.setLastActionBy(((com.example.adbridge.model.User) currentUser).getEmail());
                payment.setLastActionAt(java.time.LocalDateTime.now());
                payment.setActionReason("Cancelled by user before admin approval");
                paymentRepository.save(payment);
            });

        model.addAttribute("success", "Booking cancelled successfully");
        return "redirect:/my-bookings";
    }

    @GetMapping("/payment/{bookingId}")
    public String paymentForm(@PathVariable String bookingId, Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/payment/" + bookingId;
        }

        // Find the booking
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();

        // Check if booking has been edited
        boolean hasBeenEdited = versionHistoryService.hasBookingBeenEdited(bookingId);

        // Calculate detailed breakdown for TV advertisements, Radio promotions, and Social Media campaigns
        Map<String, Object> calculationBreakdown = new HashMap<>();
        if (booking.getServiceType().name().equals("TV_ADVERTISEMENT")) {
            calculationBreakdown = calculateTVBreakdown(booking);
        } else if (booking.getServiceType().name().equals("RADIO_PROMOTION")) {
            calculationBreakdown = calculateRadioBreakdown(booking);
        } else if (booking.getServiceType().name().equals("SOCIAL_MEDIA_CAMPAIGN")) {
            calculationBreakdown = calculateSocialMediaBreakdown(booking);
        }

        model.addAttribute("booking", booking);
        model.addAttribute("hasBeenEdited", hasBeenEdited);
        model.addAttribute("calculationBreakdown", calculationBreakdown);

        // Add social media breakdown if it's a social media campaign
        if (booking.getServiceType().name().equals("SOCIAL_MEDIA_CAMPAIGN")) {
            model.addAttribute("socialBreakdown", calculateSocialMediaBreakdown(booking));
        }

        // Add newspaper breakdown if it's a newspaper ad
        if (booking.getServiceType().name().equals("NEWSPAPER_AD")) {
            model.addAttribute("newspaperBreakdown", calculateNewspaperBreakdown(booking));
        }

        // Add billboard breakdown if it's a billboard ad
        if (booking.getServiceType().name().equals("BILLBOARD_ADVERTISING")) {
            model.addAttribute("billboardBreakdown", calculateBillboardBreakdown(booking));
        }

        // Add digital banner breakdown if it's a digital banner ad
        if (booking.getServiceType().name().equals("DIGITAL_BANNER")) {
            model.addAttribute("digitalBannerBreakdown", calculateDigitalBannerBreakdown(booking));
        }

        model.addAttribute("title", "Payment - " + booking.getServiceType());
        model.addAttribute("showBookInNav", false);
        model.addAttribute("showPaymentInNav", true);

        return "payment";
    }

    @GetMapping("/card-payment/{bookingId}")
    public String cardPaymentForm(@PathVariable String bookingId, Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/card-payment/" + bookingId;
        }

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();
        model.addAttribute("booking", booking);
        model.addAttribute("title", "Card Payment - " + booking.getServiceType());
        model.addAttribute("showBookInNav", false);
        model.addAttribute("showPaymentInNav", true);

        return "card-payment";
    }

    @GetMapping("/bank-transfer")
    public String bankTransferForm(@RequestParam String bookingId, Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/bank-transfer?bookingId=" + bookingId;
        }

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();
        model.addAttribute("booking", booking);
        model.addAttribute("title", "Bank Transfer - " + booking.getServiceType());
        model.addAttribute("showBookInNav", false);
        model.addAttribute("showPaymentInNav", true);

        return "bank-transfer";
    }

    @GetMapping("/cash-payment/{bookingId}")
    public String cashPaymentForm(@PathVariable String bookingId, Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/cash-payment/" + bookingId;
        }

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();
        model.addAttribute("booking", booking);
        model.addAttribute("title", "Cash Payment - " + booking.getServiceType());
        model.addAttribute("showBookInNav", false);
        model.addAttribute("showPaymentInNav", true);

        return "cash-payment";
    }

    @PostMapping("/process-cash-payment")
    public String processCashPayment(@RequestParam String bookingId,
                                     @RequestParam String preferredDate,
                                     @RequestParam String preferredTime,
                                     @RequestParam String paymentLocation,
                                     @RequestParam String contactNumber,
                                     @RequestParam(required = false) String specialInstructions,
                                     Model model, HttpSession session) {
        // Debug logging
        System.out.println("=== CASH PAYMENT CONTROLLER CALLED ===");
        System.out.println("Processing cash payment for booking ID: " + bookingId);
        System.out.println("Preferred Date: " + preferredDate);
        System.out.println("Preferred Time: " + preferredTime);
        System.out.println("Payment Location: " + paymentLocation);

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            System.out.println("Booking not found: " + bookingId);
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();

        // Generate unique payment ID
        String paymentId = paymentIdService.generatePaymentId(bookingId);
        System.out.println("Generated Payment ID: " + paymentId);

        // Create and save payment record
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBookingId(bookingId);
        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING_CASH);
        payment.setAmount(booking.getTotalAmount());
        payment.setCurrency("LKR");

        // Set cash payment specific fields
        payment.setPreferredDate(preferredDate);
        payment.setPreferredTime(preferredTime);
        payment.setPaymentLocation(paymentLocation);
        payment.setContactNumber(contactNumber);
        payment.setSpecialInstructions(specialInstructions);

        // Save payment to database
        paymentRepository.save(payment);
        System.out.println("Payment saved to database with ID: " + payment.getId());

        // Update booking payment status
        booking.setPaymentStatus("PENDING_CASH");
        bookingRepository.save(booking);

        // Store payment method in session for confirmation page
        session.setAttribute("paymentMethod", "cash");
        System.out.println("Set session paymentMethod to: cash");

        // Create history entry for payment
        Object currentUser = session.getAttribute("user");
        String updatedBy = currentUser != null ? currentUser.toString() : "System";
        versionHistoryService.createUpdateHistory(booking, booking, updatedBy,
                "Cash payment scheduled for " + preferredDate + " at " + preferredTime);

        System.out.println("Redirecting to confirmation page: /confirmation/" + bookingId);
        return "redirect:/confirmation/" + bookingId;
    }

    @PostMapping("/process-card-payment")
    public String processCardPayment(@RequestParam String bookingId,
                                     @RequestParam String cardNumber,
                                     @RequestParam String cardholderName,
                                     @RequestParam String expiryDate,
                                     @RequestParam String cvv,
                                     @RequestParam String billingAddress,
                                     Model model, HttpSession session) {
        // Debug logging
        System.out.println("=== CARD PAYMENT CONTROLLER CALLED ===");
        System.out.println("Processing card payment for booking ID: " + bookingId);
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Cardholder: " + cardholderName);

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            System.out.println("Booking not found: " + bookingId);
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();

        // Generate unique payment ID
        String paymentId = paymentIdService.generatePaymentId(bookingId);
        System.out.println("Generated Payment ID: " + paymentId);

        // Create and save payment record
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBookingId(bookingId);
        payment.setPaymentMethod(Payment.PaymentMethod.CARD);
        payment.setPaymentStatus(Payment.PaymentStatus.PROCESSING);
        payment.setAmount(booking.getTotalAmount());
        payment.setCurrency("LKR");

        // Set card payment specific fields
        payment.setCardNumber(cardNumber);
        payment.setCardholderName(cardholderName);
        payment.setExpiryDate(expiryDate);
        payment.setCvv(cvv);
        payment.setBillingAddress(billingAddress);

        // Save payment to database
        paymentRepository.save(payment);
        System.out.println("Payment saved to database with ID: " + payment.getId());

        // Update booking payment status
        booking.setPaymentStatus("PROCESSING");
        bookingRepository.save(booking);

        // Store payment method in session for confirmation page
        session.setAttribute("paymentMethod", "card");
        System.out.println("Set session paymentMethod to: card");

        // Create history entry for payment
        Object currentUser = session.getAttribute("user");
        String updatedBy = currentUser != null ? currentUser.toString() : "System";
        versionHistoryService.createUpdateHistory(booking, booking, updatedBy,
                "Card payment processed for " + cardholderName);

        System.out.println("Redirecting to confirmation page: /confirmation/" + bookingId);
        return "redirect:/confirmation/" + bookingId;
    }

    @PostMapping("/process-bank-transfer")
    public String processBankTransfer(@RequestParam String bookingId,
                                      @RequestParam(required = false) String bankName,
                                      @RequestParam(required = false) String accountNumber,
                                      @RequestParam(required = false) String transactionReference,
                                      @RequestParam(required = false) String paymentNotes,
                                      Model model, HttpSession session) {
        // Debug logging
        System.out.println("=== BANK TRANSFER CONTROLLER CALLED ===");
        System.out.println("Processing bank transfer for booking ID: " + bookingId);

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            System.out.println("Booking not found: " + bookingId);
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();

        // Generate unique payment ID
        String paymentId = paymentIdService.generatePaymentId(bookingId);
        System.out.println("Generated Payment ID: " + paymentId);

        // Create and save payment record
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBookingId(bookingId);
        payment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING_BANK_TRANSFER);
        payment.setAmount(booking.getTotalAmount());
        payment.setCurrency("LKR");

        // Set bank transfer specific fields
        payment.setBankName(bankName);
        payment.setAccountNumber(accountNumber);
        payment.setTransactionReference(transactionReference);
        payment.setPaymentNotes(paymentNotes);

        // Save payment to database
        paymentRepository.save(payment);
        System.out.println("Payment saved to database with ID: " + payment.getId());

        // Update booking payment status
        booking.setPaymentStatus("PENDING_BANK_TRANSFER");
        bookingRepository.save(booking);

        // Store payment method in session for confirmation page
        session.setAttribute("paymentMethod", "bank");
        System.out.println("Set session paymentMethod to: bank");

        // Create history entry for payment
        Object currentUser = session.getAttribute("user");
        String updatedBy = currentUser != null ? currentUser.toString() : "System";
        versionHistoryService.createUpdateHistory(booking, booking, updatedBy,
                "Bank transfer payment initiated");

        System.out.println("Redirecting to confirmation page: /confirmation/" + bookingId);
        return "redirect:/confirmation/" + bookingId;
    }

    @GetMapping("/confirmation/{bookingId}")
    public String confirmationPage(@PathVariable String bookingId, Model model, HttpSession session) {
        System.out.println("=== CONFIRMATION PAGE REQUESTED ===");
        System.out.println("Booking ID: " + bookingId);

        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            System.out.println("User not logged in, redirecting to login");
            return "redirect:/login?next=/confirmation/" + bookingId;
        }

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            System.out.println("Booking not found: " + bookingId);
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();
        System.out.println("Found booking: " + booking.getServiceType() + " - " + booking.getTotalAmount());
        System.out.println("Returning confirmation template");

        // Get payment method from session
        String paymentMethod = (String) session.getAttribute("paymentMethod");
        System.out.println("Payment method from session: " + paymentMethod);

        // Get payment details (payment ID and method)
        String paymentId = null;
        if (paymentMethod == null) {
            List<Payment> payments = paymentRepository.findAllByBookingId(bookingId);
            Optional<Payment> paymentOpt = payments.stream()
                .filter(payment -> payment.getDeleted() == null || !payment.getDeleted())
                .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt())); // Get most recent
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                paymentMethod = payment.getPaymentMethod().name().toLowerCase();
                paymentId = payment.getPaymentId();
                System.out.println("Payment method from database: " + paymentMethod);
                System.out.println("Payment ID from database: " + paymentId);
            } else {
                paymentMethod = "unknown";
                System.out.println("No payment method found, defaulting to: " + paymentMethod);
            }
        } else {
            // Get payment ID even if payment method is in session
            List<Payment> payments = paymentRepository.findAllByBookingId(bookingId);
            Optional<Payment> paymentOpt = payments.stream()
                .filter(payment -> payment.getDeleted() == null || !payment.getDeleted())
                .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()));
            if (paymentOpt.isPresent()) {
                paymentId = paymentOpt.get().getPaymentId();
                System.out.println("Payment ID from database: " + paymentId);
            }
        }

        model.addAttribute("booking", booking);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentId", paymentId);
        model.addAttribute("title", "Booking Confirmation - " + booking.getServiceType());
        model.addAttribute("showBookInNav", false);
        model.addAttribute("showPaymentInNav", true);

        return "confirmation";
    }

    @GetMapping("/invoice/{bookingId}")
    public String invoicePage(@PathVariable String bookingId, Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/invoice/" + bookingId;
        }

        // Get booking details
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();

        // Get payment method for this booking
        String paymentMethod = null;
        List<Payment> payments = paymentRepository.findAllByBookingId(bookingId);
        Optional<Payment> paymentOpt = payments.stream()
            .filter(payment -> payment.getDeleted() == null || !payment.getDeleted())
            .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()));
        if (paymentOpt.isPresent()) {
            paymentMethod = paymentOpt.get().getPaymentMethod().name().toLowerCase();
        }

        // Calculate detailed breakdown for TV advertisements
        Map<String, Object> calculationBreakdown = new HashMap<>();
        if (booking.getServiceType().name().equals("TV_ADVERTISEMENT")) {
            calculationBreakdown = calculateTVBreakdown(booking);
        }

        model.addAttribute("booking", booking);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("calculationBreakdown", calculationBreakdown);
        model.addAttribute("title", "Invoice - " + booking.getServiceType());
        model.addAttribute("showBookInNav", false);
        model.addAttribute("showPaymentInNav", true);

        return "invoice";
    }

    @GetMapping("/payment-details/{paymentId}")
    public String paymentDetails(@PathVariable String paymentId, Model model, HttpSession session) {
        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/payment-details/" + paymentId;
        }

        // Get payment details
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentId);
        if (paymentOpt.isEmpty()) {
            return "redirect:/book?error=payment_not_found";
        }

        Payment payment = paymentOpt.get();

        // Get associated booking
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(payment.getBookingId());
        if (bookingOpt.isEmpty()) {
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();

        model.addAttribute("payment", payment);
        model.addAttribute("booking", booking);
        model.addAttribute("title", "Payment Details - " + paymentId);
        model.addAttribute("showBookInNav", false);
        model.addAttribute("showPaymentInNav", false);

        return "payment-details";
    }

    @PostMapping("/payment/{bookingId}")
    public String processPayment(@PathVariable String bookingId,
                                 @RequestParam String paymentMethod,
                                 @RequestParam(required = false) String paymentNotes,
                                 Model model, HttpSession session) {

        // Check if user is logged in
        Object currentUser = session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login?next=/payment/" + bookingId;
        }

        // Find the booking
        Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/book?error=booking_not_found";
        }

        Booking booking = bookingOpt.get();

        // Update payment status based on method
        switch (paymentMethod.toLowerCase()) {
            case "card":
                booking.setPaymentStatus("PROCESSING");
                break;
            case "bank":
                booking.setPaymentStatus("PENDING_BANK_TRANSFER");
                break;
            case "cash":
                booking.setPaymentStatus("PENDING_CASH");
                break;
            default:
                booking.setPaymentStatus("PENDING");
        }

        // Save the updated booking
        bookingRepository.save(booking);

        // Create history entry for payment
        String updatedBy = currentUser.toString(); // You might want to get user email/name
        versionHistoryService.createUpdateHistory(booking, booking, updatedBy,
                "Payment method selected: " + paymentMethod);

        // Redirect to success page
        return "redirect:/book?success=true&payment=" + paymentMethod;
    }

    private Map<String, Object> calculateTVBreakdown(Booking booking) {
        Map<String, Object> breakdown = new HashMap<>();

        try {
            // Parse booking data
            String channels = booking.getOptionOne();
            String durationStr = booking.getOptionTwo();
            String daysStr = booking.getOptionThree();
            String timeSlots = booking.getOptionFour();

            int duration = parseInteger(durationStr, 30);
            int days = parseInteger(daysStr, 1);

            // Calculate per-second cost
            int perSecondRate = 150;
            int durationCost = duration * perSecondRate;

            // Calculate time slot costs
            int timeSlotCost = 0;
            if (timeSlots != null && !timeSlots.trim().isEmpty()) {
                String[] slots = timeSlots.split(",");
                for (String slot : slots) {
                    String trimmedSlot = slot.trim().toLowerCase();
                    switch (trimmedSlot) {
                        case "morning":
                            timeSlotCost += 1000;
                            break;
                        case "evening":
                            timeSlotCost += 2000;
                            break;
                        case "prime time":
                            timeSlotCost += 3000;
                            break;
                    }
                }
            }

            // Calculate per-day total
            int perDayTotal = durationCost + timeSlotCost;

            // Calculate per-channel total
            int perChannelTotal = perDayTotal * days;

            // Count channels
            int channelCount = 1;
            if (channels != null && !channels.trim().isEmpty()) {
                channelCount = channels.split(",").length;
                System.out.println("DEBUG: Channels string: '" + channels + "'");
                System.out.println("DEBUG: Channel count: " + channelCount);
            } else {
                System.out.println("DEBUG: No channels found, using default count: 1");
            }

            // Calculate final total
            int finalTotal = perChannelTotal * channelCount;

            // Build breakdown
            breakdown.put("perSecondRate", perSecondRate);
            breakdown.put("duration", duration);
            breakdown.put("durationCost", durationCost);
            breakdown.put("timeSlots", timeSlots);
            breakdown.put("timeSlotCost", timeSlotCost);
            breakdown.put("perDayTotal", perDayTotal);
            breakdown.put("days", days);
            breakdown.put("perChannelTotal", perChannelTotal);
            breakdown.put("channelCount", channelCount);
            breakdown.put("finalTotal", finalTotal);
            breakdown.put("channels", channels);

        } catch (Exception e) {
            System.err.println("Error calculating TV breakdown: " + e.getMessage());
            e.printStackTrace();
        }

        return breakdown;
    }

    private int parseInteger(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Map<String, Object> calculateRadioBreakdown(Booking booking) {
        Map<String, Object> breakdown = new HashMap<>();

        try {
            // Parse booking data
            String radioChannels = booking.getOptionOne();
            String durationStr = booking.getOptionTwo();
            String playsStr = booking.getOptionThree();
            String timeSlots = booking.getOptionFour();

            System.out.println("=== RADIO BREAKDOWN DEBUG ===");
            System.out.println("Radio Channels: '" + radioChannels + "'");
            System.out.println("Duration: '" + durationStr + "'");
            System.out.println("Plays: '" + playsStr + "'");
            System.out.println("Time Slots: '" + timeSlots + "'");

            int duration = parseInteger(durationStr, 15);
            int plays = parseInteger(playsStr, 10);

            // Calculate per-second cost
            int perSecondRate = 100;
            int durationCost = duration * perSecondRate;

            // Calculate time slot costs
            int timeSlotCost = 0;
            if (timeSlots != null && !timeSlots.trim().isEmpty()) {
                String[] slots = timeSlots.split(",");
                for (String slot : slots) {
                    String trimmedSlot = slot.trim().toLowerCase();
                    switch (trimmedSlot) {
                        case "morning":
                            timeSlotCost += 500;
                            break;
                        case "evening":
                            timeSlotCost += 800;
                            break;
                        case "prime time":
                            timeSlotCost += 1200;
                            break;
                    }
                }
            }

            // Calculate per-play total
            int perPlayTotal = durationCost + timeSlotCost;

            // Calculate per-channel total
            int perChannelTotal = perPlayTotal * plays;

            // Count channels
            int channelCount = 1;
            if (radioChannels != null && !radioChannels.trim().isEmpty()) {
                channelCount = radioChannels.split(",").length;
                System.out.println("DEBUG: Radio channels string: '" + radioChannels + "'");
                System.out.println("DEBUG: Radio channel count: " + channelCount);
            } else {
                System.out.println("DEBUG: No radio channels found, using default count: 1");
            }

            // Calculate final total
            int finalTotal = perChannelTotal * channelCount;

            // Build breakdown
            breakdown.put("perSecondRate", perSecondRate);
            breakdown.put("duration", duration);
            breakdown.put("durationCost", durationCost);
            breakdown.put("timeSlots", timeSlots);
            breakdown.put("timeSlotCost", timeSlotCost);
            breakdown.put("perPlayTotal", perPlayTotal);
            breakdown.put("plays", plays);
            breakdown.put("perChannelTotal", perChannelTotal);
            breakdown.put("channelCount", channelCount);
            breakdown.put("finalTotal", finalTotal);
            breakdown.put("channels", radioChannels);

        } catch (Exception e) {
            System.err.println("Error calculating Radio breakdown: " + e.getMessage());
            e.printStackTrace();
        }

        return breakdown;
    }

    private Map<String, Object> calculateSocialMediaBreakdown(Booking booking) {
        Map<String, Object> breakdown = new HashMap<>();

        try {
            // Parse booking data
            String platforms = booking.getOptionOne();
            String durationStr = booking.getOptionTwo();
            String contentTypes = booking.getOptionThree();

            System.out.println("=== SOCIAL MEDIA BREAKDOWN DEBUG ===");
            System.out.println("Platforms: '" + platforms + "'");
            System.out.println("Duration: '" + durationStr + "'");
            System.out.println("Content Types: '" + contentTypes + "'");

            int duration = parseInteger(durationStr, 7);

            // Calculate platform cost
            int platformCost = 0;
            int platformCount = 0;
            if (platforms != null && !platforms.trim().isEmpty()) {
                String[] platformArray = platforms.split(",");
                // Count only non-empty platforms
                for (String platform : platformArray) {
                    if (!platform.trim().isEmpty()) {
                        platformCount++;
                    }
                }
                platformCost = platformCount * 2500; // Rs. 2,500 per platform
            }

            // Calculate content type cost
            int contentCost = 0;
            if (contentTypes != null && !contentTypes.trim().isEmpty()) {
                String[] contentArray = contentTypes.split(",");
                for (String contentType : contentArray) {
                    String trimmed = contentType.trim();
                    if (!trimmed.isEmpty()) { // Only process non-empty values
                        switch (trimmed.toLowerCase()) {
                            case "image":
                                contentCost += 1000;
                                break;
                            case "video":
                                contentCost += 2000;
                                break;
                            case "story":
                                contentCost += 1500;
                                break;
                        }
                    }
                }
            }

            // Calculate duration cost
            int durationCost = duration * 500; // Rs. 500 per day

            // Calculate final total
            int finalTotal = platformCost + contentCost + durationCost;

            // Build breakdown
            breakdown.put("platformCost", platformCost);
            breakdown.put("platformCount", platformCount);
            breakdown.put("contentCost", contentCost);
            breakdown.put("duration", duration);
            breakdown.put("durationCost", durationCost);
            breakdown.put("finalTotal", finalTotal);
            breakdown.put("platforms", platforms);
            breakdown.put("contentTypes", contentTypes);

        } catch (Exception e) {
            System.err.println("Error calculating Social Media breakdown: " + e.getMessage());
            e.printStackTrace();
        }

        return breakdown;
    }

    private Map<String, Object> calculateNewspaperBreakdown(Booking booking) {
        Map<String, Object> breakdown = new HashMap<>();

        try {
            // Parse booking data
            String newspapers = booking.getOptionOne();
            String adSize = booking.getOptionTwo();
            String colorType = booking.getOptionThree();
            String daysStr = booking.getOptionFour();

            System.out.println("=== NEWSPAPER BREAKDOWN DEBUG ===");
            System.out.println("Newspapers: '" + newspapers + "'");
            System.out.println("Ad Size: '" + adSize + "'");
            System.out.println("Color Type: '" + colorType + "'");
            System.out.println("Days: '" + daysStr + "'");

            int days = parseInteger(daysStr, 1);

            // Calculate newspaper count
            int newspaperCount = 1;
            if (newspapers != null && newspapers.contains(",")) {
                String[] newspaperArray = newspapers.split(",");
                newspaperCount = 0;
                for (String newspaper : newspaperArray) {
                    if (!newspaper.trim().isEmpty()) {
                        newspaperCount++;
                    }
                }
            }

            // Calculate individual costs
            int sizeCost = 0;
            int colorCost = 0;

            switch (adSize.toLowerCase()) {
                case "box":
                    sizeCost = 2000;
                    break;
                case "half page":
                    sizeCost = 4000;
                    break;
                case "full page":
                    sizeCost = 6000;
                    break;
            }

            switch (colorType.toLowerCase()) {
                case "colour":
                    colorCost = 1000;
                    break;
                case "black & white":
                    colorCost = 500;
                    break;
            }

            // Calculate total cost per day per newspaper
            int costPerDayPerNewspaper = sizeCost + colorCost;
            int totalCost = costPerDayPerNewspaper * newspaperCount * days;

            // Build breakdown
            breakdown.put("newspapers", newspapers);
            breakdown.put("newspaperCount", newspaperCount);
            breakdown.put("newspaperCost", 0); // No base cost for newspapers
            breakdown.put("adSize", adSize);
            breakdown.put("colorType", colorType);
            breakdown.put("days", days);
            breakdown.put("sizeCost", sizeCost);
            breakdown.put("colorCost", colorCost);
            breakdown.put("costPerDayPerNewspaper", costPerDayPerNewspaper);
            breakdown.put("durationCost", totalCost); // This should be the total cost
            breakdown.put("finalTotal", totalCost);

            System.out.println("Newspaper count: " + newspaperCount);
            System.out.println("Size cost: " + sizeCost);
            System.out.println("Color cost: " + colorCost);
            System.out.println("Cost per day per newspaper: " + costPerDayPerNewspaper);
            System.out.println("Total cost: " + totalCost);
            System.out.println("=== END NEWSPAPER BREAKDOWN DEBUG ===");

        } catch (Exception e) {
            System.err.println("Error calculating Newspaper breakdown: " + e.getMessage());
            e.printStackTrace();
        }

        return breakdown;
    }

    private Map<String, Object> calculateBillboardBreakdown(Booking booking) {
        Map<String, Object> breakdown = new HashMap<>();

        try {
            // Parse booking data
            String locations = booking.getOptionOne();
            String billboardSize = booking.getOptionTwo();
            String daysStr = booking.getOptionThree();

            System.out.println("=== BILLBOARD BREAKDOWN DEBUG ===");
            System.out.println("Locations: '" + locations + "'");
            System.out.println("Billboard Size: '" + billboardSize + "'");
            System.out.println("Days: '" + daysStr + "'");

            int days = parseInteger(daysStr, 1);

            // Calculate size cost
            int sizeCost = 0;
            switch (billboardSize.toLowerCase()) {
                case "small":
                    sizeCost = 5000;
                    break;
                case "medium":
                    sizeCost = 7500;
                    break;
                case "large":
                    sizeCost = 10000;
                    break;
            }

            // Calculate individual location costs
            Map<String, Integer> locationCosts = new HashMap<>();
            int totalCost = 0;

            if (locations != null && !locations.trim().isEmpty()) {
                String[] locationArray = locations.split(",");
                for (String location : locationArray) {
                    String trimmedLocation = location.trim();
                    if (!trimmedLocation.isEmpty()) {
                        // Get daily rate for this specific location
                        int dailyRate = getLocationDailyRate(trimmedLocation);
                        // Calculate cost for this location: (Daily Rate × Duration) + Size Price
                        int locationCost = (dailyRate * days) + sizeCost;
                        locationCosts.put(trimmedLocation, locationCost);
                        totalCost += locationCost;

                        // Store daily rate for template display
                        breakdown.put("dailyRate_" + trimmedLocation, dailyRate);

                        System.out.println("Location: " + trimmedLocation + " - Daily Rate: " + dailyRate + " - Cost: " + locationCost);
                    }
                }
            }

            // Build breakdown
            breakdown.put("locations", locations);
            breakdown.put("billboardSize", billboardSize);
            breakdown.put("days", days);
            breakdown.put("sizeCost", sizeCost);
            breakdown.put("locationCosts", locationCosts);
            breakdown.put("finalTotal", totalCost);

            System.out.println("Size cost: " + sizeCost);
            System.out.println("Total cost: " + totalCost);
            System.out.println("=== END BILLBOARD BREAKDOWN DEBUG ===");

        } catch (Exception e) {
            System.err.println("Error calculating Billboard breakdown: " + e.getMessage());
            e.printStackTrace();
        }

        return breakdown;
    }

    // Get daily rate for specific location (same as in PricingService)
    private int getLocationDailyRate(String location) {
        switch (location.toLowerCase()) {
            case "colombo":
                return 5000;
            case "kandy":
                return 4000;
            case "galle":
                return 3500;
            case "kurunegala":
                return 3000;
            case "matara":
                return 2500;
            case "other":
                return 2000;
            default:
                return 2000; // Default for other towns
        }
    }

    // Calculate digital banner breakdown for payment display
    private Map<String, Object> calculateDigitalBannerBreakdown(Booking booking) {
        Map<String, Object> breakdown = new HashMap<>();

        String websiteType = booking.getOptionOne();
        String bannerSize = booking.getOptionTwo();
        String trackingStr = booking.getOptionFour();
        String durationStr = booking.getOptionFive();

        // Parse values
        boolean tracking = trackingStr != null && trackingStr.contains("true");
        int days = parseInteger(durationStr, 7);

        // Get size price
        int sizePrice = 0;
        switch (bannerSize.toLowerCase()) {
            case "small":
                sizePrice = 1500;
                break;
            case "medium":
                sizePrice = 2500;
                break;
            case "large":
                sizePrice = 4000;
                break;
        }

        // Get tracking price
        int trackingPrice = tracking ? 1000 : 0;

        // Daily rate
        int dailyRate = 500;
        int dailyCost = dailyRate * days;

        // Count number of selected website types
        int websiteCount = 1; // Default to 1 if no selection
        System.out.println("=== DIGITAL BANNER BREAKDOWN DEBUG ===");
        System.out.println("Raw Website Type: '" + websiteType + "'");
        if (websiteType != null && !websiteType.trim().isEmpty()) {
            String[] websiteArray = websiteType.split(",");
            websiteCount = 0;
            for (String website : websiteArray) {
                if (!website.trim().isEmpty()) {
                    websiteCount++;
                }
            }
        }
        System.out.println("Calculated Website Count: " + websiteCount);

        // Calculate cost per website
        int costPerWebsite = sizePrice + trackingPrice + dailyCost;

        // Calculate total cost
        int totalCost = costPerWebsite * websiteCount;

        // Build breakdown
        breakdown.put("websiteTypes", websiteType);
        breakdown.put("websiteCount", websiteCount);
        breakdown.put("bannerSize", bannerSize);
        breakdown.put("sizePrice", sizePrice);
        breakdown.put("tracking", tracking);
        breakdown.put("trackingPrice", trackingPrice);
        breakdown.put("dailyRate", dailyRate);
        breakdown.put("days", days);
        breakdown.put("dailyCost", dailyCost);
        breakdown.put("costPerWebsite", costPerWebsite);
        breakdown.put("totalCost", totalCost);

        return breakdown;
    }
}
