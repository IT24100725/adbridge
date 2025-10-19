package com.example.adbridge.controller;

import com.example.adbridge.model.Booking;
import com.example.adbridge.model.ServiceType;
import com.example.adbridge.model.User;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.service.PricingService;
import com.example.adbridge.service.BookingIdService;
import com.example.adbridge.service.VersionHistoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final PricingService pricingService;
    private final BookingIdService bookingIdService;
    private final VersionHistoryService versionHistoryService;

    public BookingController(BookingRepository bookingRepository, PricingService pricingService, 
                           BookingIdService bookingIdService, VersionHistoryService versionHistoryService) {
        this.bookingRepository = bookingRepository;
        this.pricingService = pricingService;
        this.bookingIdService = bookingIdService;
        this.versionHistoryService = versionHistoryService;
    }

    @ModelAttribute("serviceTypes")
    public ServiceType[] serviceTypes(){
        return ServiceType.values();
    }

    @GetMapping("/book")
    public String bookSelectionPage(Model model) {
        model.addAttribute("title", "Book a Campaign");
        model.addAttribute("showBookInNav", true);
        model.addAttribute("showPaymentInNav", false);
        return "book-select"; // Original page for service selection
    }

    @GetMapping("/book/{service}")
    public String bookFormForService(@PathVariable("service") String service,
                                     @RequestParam(value = "edit", required = false) Long editId,
                                     Model model,
                                     HttpSession session){
        // Convert service string to ServiceType enum
        ServiceType serviceType;
        try {
            // Handle different service name formats
            String enumName = service.toUpperCase();
            if (enumName.equals("TV")) {
                enumName = "TV_ADVERTISEMENT";
            } else if (enumName.equals("RADIO")) {
                enumName = "RADIO_PROMOTION";
            } else if (enumName.equals("SOCIAL")) {
                enumName = "SOCIAL_MEDIA_CAMPAIGN";
            } else if (enumName.equals("NEWSPAPER")) {
                enumName = "NEWSPAPER_AD";
            } else if (enumName.equals("BILLBOARD")) {
                enumName = "BILLBOARD_ADVERTISING";
            } else if (enumName.equals("DIGITAL")) {
                enumName = "DIGITAL_BANNER";
            }
            serviceType = ServiceType.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            return "redirect:/services";
        }
        
        model.addAttribute("title", serviceType + " Booking Form");
        model.addAttribute("showBookInNav", true);
        model.addAttribute("showPaymentInNav", false);
        
        Booking booking;
        
        // Check if this is an edit operation
        if (editId != null) {
            // Load existing booking for editing
            Optional<Booking> existingBookingOpt = bookingRepository.findById(editId);
            if (existingBookingOpt.isPresent()) {
                Booking existingBooking = existingBookingOpt.get();
                // Create a new booking object with only personal details from existing booking
                booking = new Booking();
                booking.setServiceType(existingBooking.getServiceType());
                // Only copy personal details, not service-specific options
                booking.setFullName(existingBooking.getFullName());
                booking.setCompanyName(existingBooking.getCompanyName());
                booking.setContactNumber(existingBooking.getContactNumber());
                booking.setEmail(existingBooking.getEmail());
                model.addAttribute("isEdit", true);
                model.addAttribute("editId", editId);
            } else {
                // If booking not found, create new one
                booking = new Booking();
                booking.setServiceType(serviceType);
            }
        } else {
            booking = new Booking();
            // Set the service type
            booking.setServiceType(serviceType);
            
            // Auto-fill user details if logged in
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                booking.setFullName(currentUser.getFullName());
                booking.setEmail(currentUser.getEmail());
                booking.setContactNumber(currentUser.getPhone());
                booking.setCompanyName(currentUser.getCompanyName());
            }
            
            // Check if there's saved booking data in session (for non-logged users)
            Booking savedBooking = (Booking) session.getAttribute("savedBookingData");
            if (savedBooking != null && savedBooking.getServiceType() == booking.getServiceType()) {
                // Restore saved form data
                booking.setFullName(savedBooking.getFullName());
                booking.setCompanyName(savedBooking.getCompanyName());
                booking.setContactNumber(savedBooking.getContactNumber());
                booking.setEmail(savedBooking.getEmail());
                booking.setOptionOne(savedBooking.getOptionOne());
                booking.setOptionTwo(savedBooking.getOptionTwo());
                booking.setOptionThree(savedBooking.getOptionThree());
                booking.setOptionFour(savedBooking.getOptionFour());
                booking.setOptionFive(savedBooking.getOptionFive());
                booking.setNotes(savedBooking.getNotes());
                
                // Clear saved data after restoring
                session.removeAttribute("savedBookingData");
            }
        }
        
        model.addAttribute("lockedService", true);
        model.addAttribute("booking", booking);
        return "book";
    }

    @PostMapping("/book")
    public String submitBooking(@Valid @ModelAttribute("booking") Booking booking,
                                @RequestParam(value = "editId", required = false) Long editId,
                                BindingResult bindingResult,
                                Model model,
                                HttpSession session){
        model.addAttribute("title", "Book a Campaign");
        model.addAttribute("showBookInNav", true);
        
        // Check if user is logged in
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            // User is not logged in - save form data and redirect to login
            session.setAttribute("savedBookingData", booking);
            session.setAttribute("redirectAfterLogin", "/book/" + booking.getServiceType().name());
            return "redirect:/login?next=/book/" + booking.getServiceType().name();
        }
        
        if(bindingResult.hasErrors()){
            // If there are errors, ensure lockedService is re-added to the model
            if (booking.getServiceType() != null) {
                model.addAttribute("lockedService", true);
                model.addAttribute("title", booking.getServiceType() + " Booking Form");
            }
            if (editId != null) {
                model.addAttribute("isEdit", true);
                model.addAttribute("editId", editId);
            }
            return "book";
        }
        
        // Debug: Print what's being submitted
        System.out.println("=== FORM SUBMISSION DEBUG ===");
        System.out.println("Edit ID: " + editId);
        System.out.println("Option One: " + booking.getOptionOne());
        System.out.println("Option Two: " + booking.getOptionTwo());
        System.out.println("Option Three: " + booking.getOptionThree());
        System.out.println("Option Four: " + booking.getOptionFour());
        System.out.println("Option Five: " + booking.getOptionFive());
        
        // Debug social media specific data
        if (booking.getServiceType() != null && booking.getServiceType().name().equals("SOCIAL_MEDIA_CAMPAIGN")) {
            System.out.println("=== SOCIAL MEDIA DEBUG ===");
            System.out.println("Platforms: '" + booking.getOptionOne() + "'");
            System.out.println("Duration: '" + booking.getOptionTwo() + "'");
            System.out.println("Content Types: '" + booking.getOptionThree() + "'");
        }
        
        // Clean up form data before processing
        cleanBookingDataForSave(booking);
        
        Booking savedBooking;
        
        // Check if this is an edit operation
        if (editId != null) {
            System.out.println("=== EDITING EXISTING BOOKING ===");
            System.out.println("Edit ID: " + editId);
            
            // Load the existing booking
            Optional<Booking> existingBookingOpt = bookingRepository.findById(editId);
            if (existingBookingOpt.isEmpty()) {
                System.err.println("ERROR: Booking not found for editId: " + editId);
                return "redirect:/book?error=booking_not_found";
            }
            
            Booking existingBooking = existingBookingOpt.get();
            
            // Preserve the original booking ID and other important fields
            String originalBookingId = existingBooking.getBookingId();
            String originalPaymentStatus = existingBooking.getPaymentStatus();
            String originalCampaignStatus = existingBooking.getCampaignStatus();
            Integer originalCurrentVersion = existingBooking.getCurrentVersion();
            Boolean originalIsEdited = existingBooking.getIsEdited();
            
            // Copy the updated data to the existing booking
            existingBooking.setFullName(booking.getFullName());
            existingBooking.setCompanyName(booking.getCompanyName());
            existingBooking.setContactNumber(booking.getContactNumber());
            existingBooking.setEmail(booking.getEmail());
            existingBooking.setServiceType(booking.getServiceType());
            existingBooking.setOptionOne(booking.getOptionOne());
            existingBooking.setOptionTwo(booking.getOptionTwo());
            existingBooking.setOptionThree(booking.getOptionThree());
            existingBooking.setOptionFour(booking.getOptionFour());
            existingBooking.setOptionFive(booking.getOptionFive());
            existingBooking.setNotes(booking.getNotes());
            
            // Recalculate pricing for the edited booking
            calculateAndSetPricingForEdit(existingBooking);
            
            // Update version tracking for edit
            existingBooking.setLastModifiedAt(java.time.LocalDateTime.now());
            existingBooking.setIsEdited(true);
            // Increment version number for edits
            if (originalCurrentVersion != null) {
                existingBooking.setCurrentVersion(originalCurrentVersion + 1);
            } else {
                existingBooking.setCurrentVersion(2); // Start at version 2 for first edit
            }
            
            // Preserve original statuses
            existingBooking.setPaymentStatus(originalPaymentStatus);
            existingBooking.setCampaignStatus(originalCampaignStatus);
            
            // Save the updated booking
            savedBooking = bookingRepository.save(existingBooking);
            
            // Create history entry for the edit
            String updatedBy = currentUser != null ? currentUser.getEmail() : "anonymous";
            versionHistoryService.createUpdateHistory(existingBooking, savedBooking, updatedBy, "Booking edited by user");
            
            System.out.println("Updated existing booking with ID: " + savedBooking.getBookingId());
        } else {
            System.out.println("=== CREATE NEW BOOKING ===");
            // Generate unique booking ID
            String uniqueBookingId = bookingIdService.generateUniqueBookingId();
            booking.setBookingId(uniqueBookingId);
            System.out.println("Generated Booking ID: " + uniqueBookingId);
            
            // Calculate pricing before saving
            calculateAndSetPricing(booking);
            
            // Set initial version tracking
            booking.setCurrentVersion(1);
            booking.setLastModifiedAt(java.time.LocalDateTime.now());
            booking.setIsEdited(false);
            booking.setPaymentStatus("PENDING");
            
            savedBooking = bookingRepository.save(booking);
            
            // Create initial history entry
            String createdBy = currentUser != null ? currentUser.getEmail() : "anonymous";
            versionHistoryService.createInitialHistory(savedBooking, createdBy);
        }
        
        // Redirect to payment form
        return "redirect:/payment/" + savedBooking.getBookingId();
    }
    
    
    private String cleanCommaSeparatedValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        // If it has multiple values, take ONLY the last one (most recent)
        if (value.contains(",")) {
            String[] parts = value.split(",");
            for (int i = parts.length - 1; i >= 0; i--) {
                String trimmed = parts[i].trim();
                if (!trimmed.isEmpty()) {
                    return trimmed; // Return the LAST (most recent) value only
                }
            }
        }
        
        return value.trim();
    }
    
    private String cleanMultipleValues(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        // For multiple values, keep ALL non-empty values
        if (value.contains(",")) {
            String[] parts = value.split(",");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    if (result.length() > 0) {
                        result.append(",");
                    }
                    result.append(trimmed);
                }
            }
            return result.toString();
        }
        
        return value.trim();
    }
    
    private String cleanAccumulatedValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        // Remove accumulated values and keep only unique values
        if (value.contains(",")) {
            String[] parts = value.split(",");
            Set<String> uniqueValues = new LinkedHashSet<>();
            
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    uniqueValues.add(trimmed);
                }
            }
            
            return String.join(",", uniqueValues);
        }
        
        return value.trim();
    }
    
    private String extractCleanTimeSlots(String accumulatedValue) {
        if (accumulatedValue == null || accumulatedValue.trim().isEmpty()) {
            return "";
        }
        
        System.out.println("=== EXTRACTING CLEAN TIME SLOTS ===");
        System.out.println("Input: " + accumulatedValue);
        
        // NUCLEAR OPTION: For edit mode, we need to get the ACTUAL current selection
        // The form sends accumulated data, but we need to extract what was actually selected
        
        // Split by comma and get unique values
        String[] parts = accumulatedValue.split(",");
        Set<String> uniqueValues = new LinkedHashSet<>();
        
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                uniqueValues.add(trimmed);
            }
        }
        
        String result = String.join(",", uniqueValues);
        System.out.println("Output: " + result);
        
        return result;
    }
    
    private void cleanBookingDataForSave(Booking booking) {
        // Clean up form data to prevent comma accumulation
        // For TV channels, Radio channels, Social Media platforms, Newspapers, and Billboard locations (optionOne), keep ALL values for multiple selection
        if (booking.getServiceType() != null && 
            (booking.getServiceType().name().equals("TV_ADVERTISEMENT") || 
             booking.getServiceType().name().equals("RADIO_PROMOTION") ||
             booking.getServiceType().name().equals("SOCIAL_MEDIA_CAMPAIGN") ||
             booking.getServiceType().name().equals("NEWSPAPER_AD") ||
             booking.getServiceType().name().equals("BILLBOARD_ADVERTISING") ||
             booking.getServiceType().name().equals("DIGITAL_BANNER"))) {
            // Keep all values for TV, Radio, Social Media, Newspaper, and Billboard services
            booking.setOptionOne(cleanMultipleValues(booking.getOptionOne()));
        } else {
            booking.setOptionOne(cleanCommaSeparatedValue(booking.getOptionOne()));
        }
        
        booking.setOptionTwo(cleanCommaSeparatedValue(booking.getOptionTwo()));
        
        // For Social Media content types (optionThree), keep ALL values for multiple selection
        if (booking.getServiceType() != null && 
            booking.getServiceType().name().equals("SOCIAL_MEDIA_CAMPAIGN")) {
            booking.setOptionThree(cleanMultipleValues(booking.getOptionThree()));
        } else {
            booking.setOptionThree(cleanCommaSeparatedValue(booking.getOptionThree()));
        }
        
        // For time slots (optionFour), keep ALL values, don't clean
        // But for newspaper ads, optionFour contains days, so clean it
        if (booking.getServiceType() != null && 
            booking.getServiceType().name().equals("NEWSPAPER_AD")) {
            booking.setOptionFour(cleanCommaSeparatedValue(booking.getOptionFour()));
        }
        // booking.setOptionFour(cleanCommaSeparatedValue(booking.getOptionFour()));
        booking.setOptionFive(cleanCommaSeparatedValue(booking.getOptionFive()));
    }
    
    private void calculateAndSetPricingForEdit(Booking booking) {
        int totalAmount = 0;
        String serviceType = booking.getServiceType().name().toLowerCase();
        
        // Debug: Print edited booking data
        System.out.println("=== DEBUGGING EDITED BOOKING DATA ===");
        System.out.println("Service Type: " + serviceType);
        System.out.println("Option One: " + booking.getOptionOne());
        System.out.println("Option Two: " + booking.getOptionTwo());
        System.out.println("Option Three: " + booking.getOptionThree());
        System.out.println("Option Four: " + booking.getOptionFour());
        System.out.println("Option Five: " + booking.getOptionFive());
        
        switch (serviceType) {
            case "tv":
            case "tv_advertisement":
                // For TV: editedOptionOne = TV Channels (comma-separated), editedOptionTwo = duration, editedOptionThree = days, editedOptionFour = time slots
                String tvChannels = booking.getOptionOne();
                String durationStr = booking.getOptionTwo();
                String daysStr = booking.getOptionThree();
                String timeSlot = booking.getOptionFour();
                
                // Convert string values to integers
                int duration = parseInteger(durationStr, 30); // default 30 seconds
                int days = parseInteger(daysStr, 1); // default 1 day
                
                System.out.println("TV Calculation - Channels: " + tvChannels + ", TimeSlot: " + timeSlot + ", Duration: " + duration + ", Days: " + days);
                
                // Calculate pricing for each selected channel
                if (tvChannels != null && !tvChannels.trim().isEmpty()) {
                    String[] channels = tvChannels.split(",");
                    for (String channel : channels) {
                        String trimmedChannel = channel.trim();
                        if (!trimmedChannel.isEmpty()) {
                            // Calculate price for this specific channel
                            int channelPrice = pricingService.calculateTVAdvertisement(timeSlot, duration, days);
                            totalAmount += channelPrice;
                            System.out.println("Edit - Channel: " + trimmedChannel + " - Price: " + channelPrice);
                        }
                    }
                } else {
                    // Default to one channel if none selected
                    totalAmount = pricingService.calculateTVAdvertisement(timeSlot, duration, days);
                }
                
                System.out.println("TV Edit Total Amount: " + totalAmount);
                break;
            case "radio":
            case "radio_promotion":
                // For Radio: editedOptionOne = radio channels, editedOptionTwo = duration, editedOptionThree = plays, editedOptionFour = time slot
                String radioChannels = booking.getOptionOne();
                String radioDurationStr = booking.getOptionTwo();
                String playsStr = booking.getOptionThree();
                String radioTimeSlot = booking.getOptionFour();
                
                // Convert string values to integers
                int radioDuration = parseInteger(radioDurationStr, 15); // default 15 seconds
                int plays = parseInteger(playsStr, 10); // default 10 plays
                
                // Use multi-channel pricing if multiple channels are selected
                if (radioChannels != null && radioChannels.contains(",")) {
                    totalAmount = pricingService.calculateRadioPromotionMultiChannel(radioChannels, radioTimeSlot, radioDuration, plays);
                } else {
                    totalAmount = pricingService.calculateRadioPromotion(radioTimeSlot, radioDuration, plays);
                }
                break;
            case "social_media":
            case "social":
            case "social_media_campaign":
                // For Social Media: editedOptionOne = platforms, editedOptionTwo = duration, editedOptionThree = content types
                String platforms = booking.getOptionOne();
                String socialDurationStr = booking.getOptionTwo();
                String contentTypes = booking.getOptionThree();

                System.out.println("=== SOCIAL MEDIA EDIT PRICING DEBUG ===");
                System.out.println("Platforms: '" + platforms + "'");
                System.out.println("Content Types: '" + contentTypes + "'");
                System.out.println("Duration: '" + socialDurationStr + "'");

                // Convert string values to integers and extract platforms
                int socialDuration = parseInteger(socialDurationStr, 7); // default 7 days
                String[] platformArray = extractPlatforms(platforms);
                String[] contentTypesArray = extractContentTypes(contentTypes);

                totalAmount = pricingService.calculateSocialMediaCampaign(platformArray, contentTypesArray, socialDuration);
                System.out.println("Social Media Edit Total Amount: " + totalAmount);
                break;

            case "newspaper":
            case "newspaper_ad":
                // For Newspaper: editedOptionOne = newspapers, editedOptionTwo = ad size, editedOptionThree = color type, editedOptionFour = days
                String newspapersAd = booking.getOptionOne();
                String adSizeNewspaper = booking.getOptionTwo();
                String colorTypeNewspaper = booking.getOptionThree();
                String printDaysStrNewspaper = booking.getOptionFour();

                // Convert string values to integers
                int printDaysNewspaper = parseInteger(printDaysStrNewspaper, 1); // default 1 day

                System.out.println("=== NEWSPAPER EDIT PRICING DEBUG ===");
                System.out.println("Newspapers: '" + newspapersAd + "'");
                System.out.println("Ad Size: '" + adSizeNewspaper + "'");
                System.out.println("Color Type: '" + colorTypeNewspaper + "'");
                System.out.println("Days: " + printDaysNewspaper);

                totalAmount = pricingService.calculateNewspaperAd(newspapersAd, adSizeNewspaper, colorTypeNewspaper, printDaysNewspaper);
                System.out.println("Newspaper Edit Total Amount: " + totalAmount);
                break;

            case "billboard":
            case "billboard_advertising":
                // For Billboard: editedOptionOne = locations, editedOptionTwo = size, editedOptionThree = days
                String billboardLocations = booking.getOptionOne();
                String billboardSize = booking.getOptionTwo();
                String billboardDaysStr = booking.getOptionThree();
                
                // Convert string values to integers
                int billboardDays = parseInteger(billboardDaysStr, 1); // default 1 day
                
                // Use billboard advertising method for billboard advertising
                totalAmount = pricingService.calculateBillboardAdvertising(billboardLocations, billboardSize, billboardDays);
                break;
                
            case "digital":
            case "digital_banner":
                String websiteType = booking.getOptionOne();
                String bannerSize = booking.getOptionTwo();
                String trackingStr = booking.getOptionFour();
                String bannerDurationStr = booking.getOptionFive();
                
                boolean tracking = trackingStr != null && trackingStr.contains("true");
                int bannerDuration = parseInteger(bannerDurationStr, 7);
                
                totalAmount = pricingService.calculateDigitalBanner(websiteType, bannerSize, tracking, bannerDuration);
                break;
                
            default:
                System.out.println("Unknown service type: " + serviceType);
                totalAmount = 0;
        }
        
        // Set the pricing
        booking.setTotalAmount(totalAmount);
        System.out.println("=== FINAL EDITED TOTAL AMOUNT: " + totalAmount + " ===");
    }
    
    private String extractFirstValue(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        // Split by comma and get the first non-empty value
        String[] parts = value.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return defaultValue;
    }
    
    private String[] extractPlatforms(String value) {
        System.out.println("=== EXTRACT PLATFORMS DEBUG ===");
        System.out.println("Input value: '" + value + "'");
        
        if (value == null || value.trim().isEmpty()) {
            System.out.println("Value is null or empty, returning default Facebook");
            return new String[]{"Facebook"};
        }
        
        // Split by comma and filter out empty values
        String[] parts = value.split(",");
        System.out.println("Split parts: " + java.util.Arrays.toString(parts));
        
        java.util.List<String> platforms = new java.util.ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            System.out.println("Processing part: '" + part + "' -> trimmed: '" + trimmed + "'");
            if (!trimmed.isEmpty()) {
                platforms.add(trimmed);
                System.out.println("Added platform: '" + trimmed + "'");
            }
        }
        
        String[] result = platforms.isEmpty() ? new String[]{"Facebook"} : platforms.toArray(new String[0]);
        System.out.println("Final platforms array: " + java.util.Arrays.toString(result));
        System.out.println("=== END EXTRACT PLATFORMS DEBUG ===");
        return result;
    }
    
    private String[] extractContentTypes(String value) {
        System.out.println("=== EXTRACT CONTENT TYPES DEBUG ===");
        System.out.println("Input value: '" + value + "'");
        
        if (value == null || value.trim().isEmpty()) {
            System.out.println("Value is null or empty, returning default Image");
            return new String[]{"Image"};
        }
        
        // Split by comma and filter out empty values
        String[] parts = value.split(",");
        System.out.println("Split parts: " + java.util.Arrays.toString(parts));
        
        java.util.List<String> contentTypes = new java.util.ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            System.out.println("Processing part: '" + part + "' -> trimmed: '" + trimmed + "'");
            if (!trimmed.isEmpty()) {
                contentTypes.add(trimmed);
                System.out.println("Added content type: '" + trimmed + "'");
            }
        }
        
        String[] result = contentTypes.isEmpty() ? new String[]{"Image"} : contentTypes.toArray(new String[0]);
        System.out.println("Final content types array: " + java.util.Arrays.toString(result));
        System.out.println("=== END EXTRACT CONTENT TYPES DEBUG ===");
        return result;
    }
    
    private int parseInteger(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        // Handle comma-separated values - take the last non-empty value
        if (value.contains(",")) {
            String[] parts = value.split(",");
            for (int i = parts.length - 1; i >= 0; i--) {
                String trimmed = parts[i].trim();
                if (!trimmed.isEmpty()) {
                    try {
                        return Integer.parseInt(trimmed);
                    } catch (NumberFormatException e) {
                        // Continue to next part
                    }
                }
            }
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Calculate and set pricing for a booking based on service type
     */
    private void calculateAndSetPricing(Booking booking) {
        int totalAmount = 0;
        String serviceType = booking.getServiceType().name().toLowerCase();
        
        System.out.println("=== CALCULATING PRICING FOR: " + serviceType + " ===");
        
        switch (serviceType) {
            case "tv":
            case "tv_advertisement":
                String tvChannels = booking.getOptionOne();
                String durationStr = booking.getOptionTwo();
                String daysStr = booking.getOptionThree();
                String timeSlot = booking.getOptionFour();
                
                int duration = parseInteger(durationStr, 30);
                int days = parseInteger(daysStr, 1);
                
                // Calculate pricing for each selected channel
                System.out.println("DEBUG: TV Channels received: '" + tvChannels + "'");
                if (tvChannels != null && !tvChannels.trim().isEmpty()) {
                    String[] channels = tvChannels.split(",");
                    System.out.println("DEBUG: Split channels count: " + channels.length);
                    for (String channel : channels) {
                        String trimmedChannel = channel.trim();
                        if (!trimmedChannel.isEmpty()) {
                            // Calculate price for this specific channel
                            int channelPrice = pricingService.calculateTVAdvertisement(timeSlot, duration, days);
                            totalAmount += channelPrice;
                            System.out.println("Channel: " + trimmedChannel + " - Price: " + channelPrice);
                        }
                    }
                } else {
                    // Default to one channel if none selected
                    System.out.println("DEBUG: No channels found, using default calculation");
                    totalAmount = pricingService.calculateTVAdvertisement(timeSlot, duration, days);
                }
                
                System.out.println("TV Total Amount: " + totalAmount);
                break;
                
            case "radio":
            case "radio_promotion":
                String radioChannels = booking.getOptionOne();
                String radioDurationStr = booking.getOptionTwo();
                String playsStr = booking.getOptionThree();
                String radioTimeSlot = booking.getOptionFour();
                
                System.out.println("=== RADIO BOOKING DEBUG ===");
                System.out.println("Radio Channels: '" + radioChannels + "'");
                System.out.println("Duration: '" + radioDurationStr + "'");
                System.out.println("Plays: '" + playsStr + "'");
                System.out.println("Time Slot: '" + radioTimeSlot + "'");
                
                int radioDuration = parseInteger(radioDurationStr, 15);
                int plays = parseInteger(playsStr, 10);
                
                // Use multi-channel pricing if multiple channels are selected
                if (radioChannels != null && radioChannels.contains(",")) {
                    System.out.println("Using multi-channel pricing for " + radioChannels.split(",").length + " channels");
                    totalAmount = pricingService.calculateRadioPromotionMultiChannel(radioChannels, radioTimeSlot, radioDuration, plays);
                } else {
                    System.out.println("Using single-channel pricing");
                    totalAmount = pricingService.calculateRadioPromotion(radioTimeSlot, radioDuration, plays);
                }
                System.out.println("Radio Total Amount: " + totalAmount);
                break;
                
            case "social":
            case "social_media_campaign":
                String platforms = booking.getOptionOne();
                String contentTypes = booking.getOptionThree(); // Content types are now in optionThree
                String socialDurationStr = booking.getOptionTwo(); // Duration is now in optionTwo
                
                System.out.println("=== SOCIAL MEDIA PRICING DEBUG ===");
                System.out.println("Raw Platforms: '" + platforms + "'");
                System.out.println("Raw Content Types: '" + contentTypes + "'");
                System.out.println("Raw Duration: '" + socialDurationStr + "'");
                
                int socialDuration = parseInteger(socialDurationStr, 7);
                String[] platformArray = extractPlatforms(platforms);
                String[] contentTypesArray = extractContentTypes(contentTypes);
                
                System.out.println("Parsed Platforms: " + java.util.Arrays.toString(platformArray));
                System.out.println("Parsed Content Types: " + java.util.Arrays.toString(contentTypesArray));
                System.out.println("Parsed Duration: " + socialDuration);
                
                totalAmount = pricingService.calculateSocialMediaCampaign(platformArray, contentTypesArray, socialDuration);
                System.out.println("Calculated Total: " + totalAmount);
                break;
                
            case "newspaper":
            case "newspaper_ad":
                String newspapers = booking.getOptionOne();
                String adSize = booking.getOptionTwo();
                String colorType = booking.getOptionThree();
                String printDaysStr = booking.getOptionFour();
                
                System.out.println("=== NEWSPAPER PRICING DEBUG ===");
                System.out.println("Raw Newspapers: '" + newspapers + "'");
                System.out.println("Raw Ad Size: '" + adSize + "'");
                System.out.println("Raw Color Type: '" + colorType + "'");
                System.out.println("Raw Days String: '" + printDaysStr + "'");
                
                int printDays = parseInteger(printDaysStr, 1);
                System.out.println("Parsed Days: " + printDays);
                
                totalAmount = pricingService.calculateNewspaperAd(newspapers, adSize, colorType, printDays);
                System.out.println("Calculated Total: " + totalAmount);
                break;
                
            case "billboard":
            case "billboard_advertising":
                String locations = booking.getOptionOne();
                String billboardSize = booking.getOptionTwo();
                String billboardDaysStr = booking.getOptionThree();
                
                System.out.println("=== BILLBOARD PRICING DEBUG ===");
                System.out.println("Raw Locations: '" + locations + "'");
                System.out.println("Raw Billboard Size: '" + billboardSize + "'");
                System.out.println("Raw Days String: '" + billboardDaysStr + "'");
                
                int billboardDays = parseInteger(billboardDaysStr, 1);
                System.out.println("Parsed Days: " + billboardDays);
                
                totalAmount = pricingService.calculateBillboardAdvertising(locations, billboardSize, billboardDays);
                System.out.println("Calculated Total: " + totalAmount);
                break;
                
            case "digital":
            case "digital_banner":
                String websiteType = booking.getOptionOne();
                String bannerSize = booking.getOptionTwo();
                String trackingStr = booking.getOptionFour();
                String bannerDurationStr = booking.getOptionFive();
                
                System.out.println("=== DIGITAL BANNER PRICING DEBUG ===");
                System.out.println("Raw Website Type: '" + websiteType + "'");
                System.out.println("Raw Banner Size: '" + bannerSize + "'");
                System.out.println("Raw Tracking String: '" + trackingStr + "'");
                System.out.println("Raw Duration String: '" + bannerDurationStr + "'");
                
                boolean tracking = trackingStr != null && trackingStr.contains("true");
                int bannerDuration = parseInteger(bannerDurationStr, 7);
                
                System.out.println("Parsed Tracking: " + tracking);
                System.out.println("Parsed Duration: " + bannerDuration);
                
                // Debug website count calculation
                if (websiteType != null && !websiteType.trim().isEmpty()) {
                    String[] websiteArray = websiteType.split(",");
                    int debugCount = 0;
                    for (String website : websiteArray) {
                        if (!website.trim().isEmpty()) {
                            debugCount++;
                        }
                    }
                    System.out.println("Debug Website Count: " + debugCount);
                }
                
                totalAmount = pricingService.calculateDigitalBanner(websiteType, bannerSize, tracking, bannerDuration);
                System.out.println("Calculated Total: " + totalAmount);
                break;
        }
        
        booking.setTotalAmount(totalAmount);
        System.out.println("=== FINAL TOTAL AMOUNT: " + totalAmount + " ===");
    }
}






