package com.example.adbridge.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class PricingService {
    
    // TV Advertisement Pricing
    public int calculateTVAdvertisement(String timeSlot, int duration, int days) {
        int basePrice = 150; // Per second
        int timeSlotPrice = 0;
        
        // Handle multiple time slots (comma-separated)
        if (timeSlot != null && timeSlot.contains(",")) {
            String[] slots = timeSlot.split(",");
            for (String slot : slots) {
                String trimmedSlot = slot.trim().toLowerCase();
                switch (trimmedSlot) {
                    case "morning":
                        timeSlotPrice += 1000;
                        break;
                    case "evening":
                        timeSlotPrice += 2000;
                        break;
                    case "prime time":
                        timeSlotPrice += 3000;
                        break;
                }
            }
        } else {
            // Single time slot
            switch (timeSlot.toLowerCase()) {
                case "morning":
                    timeSlotPrice = 1000;
                    break;
                case "evening":
                    timeSlotPrice = 2000;
                    break;
                case "prime time":
                    timeSlotPrice = 3000;
                    break;
            }
        }
        
        int totalPerDay = (basePrice * duration) + timeSlotPrice;
        return totalPerDay * days;
    }
    
    // Radio Promotion Pricing
    public int calculateRadioPromotion(String timeSlot, int duration, int plays) {
        int basePrice = 100; // Per second
        int timeSlotPrice = 0;
        
        System.out.println("Radio Pricing - TimeSlot: " + timeSlot + ", Duration: " + duration + ", Plays: " + plays);
        
        switch (timeSlot.toLowerCase()) {
            case "morning":
                timeSlotPrice = 500;
                break;
            case "evening":
                timeSlotPrice = 800;
                break;
            case "prime time":
                timeSlotPrice = 1200;
                break;
            default:
                timeSlotPrice = 500; // Default to morning
                break;
        }
        
        int totalPerPlay = (basePrice * duration) + timeSlotPrice;
        int total = totalPerPlay * plays;
        
        System.out.println("Radio Pricing - Base: " + basePrice + ", TimeSlot: " + timeSlotPrice + ", PerPlay: " + totalPerPlay + ", Total: " + total);
        
        return total;
    }
    
    // Radio Promotion Pricing with Multiple Channels
    public int calculateRadioPromotionMultiChannel(String radioChannels, String timeSlot, int duration, int plays) {
        int basePrice = 100; // Per second
        int timeSlotPrice = 0;
        
        // Count number of selected radio channels
        String[] channels = radioChannels.split(",");
        int channelCount = channels.length;
        
        System.out.println("Radio Multi-Channel Pricing - Channels: " + radioChannels + ", ChannelCount: " + channelCount + ", TimeSlot: " + timeSlot + ", Duration: " + duration + ", Plays: " + plays);
        
        // Handle multiple time slots - calculate average or use highest
        if (timeSlot != null && timeSlot.contains(",")) {
            String[] timeSlots = timeSlot.split(",");
            int totalTimeSlotPrice = 0;
            for (String slot : timeSlots) {
                String trimmedSlot = slot.trim().toLowerCase();
                switch (trimmedSlot) {
                    case "morning":
                        totalTimeSlotPrice += 500;
                        break;
                    case "evening":
                        totalTimeSlotPrice += 800;
                        break;
                    case "prime time":
                        totalTimeSlotPrice += 1200;
                        break;
                }
            }
            timeSlotPrice = totalTimeSlotPrice;
        } else {
            // Single time slot
            switch (timeSlot.toLowerCase()) {
                case "morning":
                    timeSlotPrice = 500;
                    break;
                case "evening":
                    timeSlotPrice = 800;
                    break;
                case "prime time":
                    timeSlotPrice = 1200;
                    break;
                default:
                    timeSlotPrice = 500; // Default to morning
                    break;
            }
        }
        
        int totalPerPlay = (basePrice * duration) + timeSlotPrice;
        int totalPerChannel = totalPerPlay * plays;
        int total = totalPerChannel * channelCount;
        
        System.out.println("Radio Multi-Channel Pricing - Base: " + basePrice + ", TimeSlot: " + timeSlotPrice + ", PerPlay: " + totalPerPlay + ", PerChannel: " + totalPerChannel + ", Total: " + total);
        
        return total;
    }
    
    // Social Media Campaign Pricing
    public int calculateSocialMediaCampaign(String[] platforms, String[] contentTypes, int duration) {
        int platformPrice = 2500; // Per platform
        int dailyPrice = 500; // Per day
        
        System.out.println("=== PRICING SERVICE DEBUG ===");
        System.out.println("Platforms array: " + java.util.Arrays.toString(platforms));
        System.out.println("Content types array: " + java.util.Arrays.toString(contentTypes));
        System.out.println("Duration: " + duration);
        
        // Calculate platform cost
        int totalPlatforms = platforms.length;
        int platformCost = platformPrice * totalPlatforms;
        System.out.println("Platform count: " + totalPlatforms + ", Cost: " + platformCost);
        
        // Calculate content type cost (sum of all selected content types)
        int contentCost = 0;
        for (String contentType : contentTypes) {
            System.out.println("Processing content type: '" + contentType + "'");
            switch (contentType.toLowerCase()) {
                case "image":
                    contentCost += 1000;
                    System.out.println("Added 1000 for Image, total content cost: " + contentCost);
                    break;
                case "video":
                    contentCost += 2000;
                    System.out.println("Added 2000 for Video, total content cost: " + contentCost);
                    break;
                case "story":
                    contentCost += 1500;
                    System.out.println("Added 1500 for Story, total content cost: " + contentCost);
                    break;
            }
        }
        
        // Calculate duration cost
        int durationCost = dailyPrice * duration;
        System.out.println("Duration cost: " + durationCost);
        
        int total = platformCost + contentCost + durationCost;
        System.out.println("FINAL TOTAL: " + total);
        System.out.println("=== END PRICING SERVICE DEBUG ===");
        
        return total;
    }
    
    // Newspaper Ad Pricing
    public int calculateNewspaperAd(String newspapers, String adSize, String colorType, int days) {
        System.out.println("=== NEWSPAPER PRICING DEBUG ===");
        System.out.println("Newspapers: '" + newspapers + "'");
        System.out.println("Ad Size: '" + adSize + "'");
        System.out.println("Color Type: '" + colorType + "'");
        System.out.println("Days: " + days);
        
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
        
        System.out.println("Newspaper count: " + newspaperCount);
        
        int sizePrice = 0;
        int colorPrice = 0;
        
        switch (adSize.toLowerCase()) {
            case "box":
                sizePrice = 2000;
                break;
            case "half page":
                sizePrice = 4000;
                break;
            case "full page":
                sizePrice = 6000;
                break;
        }
        
        switch (colorType.toLowerCase()) {
            case "colour":
                colorPrice = 1000;
                break;
            case "black & white":
                colorPrice = 500;
                break;
        }
        
        int totalPerDay = (sizePrice + colorPrice) * newspaperCount;
        int totalCost = totalPerDay * days;
        
        System.out.println("Size price: " + sizePrice);
        System.out.println("Color price: " + colorPrice);
        System.out.println("Total per day: " + totalPerDay);
        System.out.println("Total cost: " + totalCost);
        System.out.println("=== END NEWSPAPER PRICING DEBUG ===");
        
        return totalCost;
    }
    
    // Billboard Advertising Pricing
    public int calculateBillboardAdvertising(String locations, String billboardSize, int duration) {
        int sizePrice = 0;
        
        // Get billboard size price
        switch (billboardSize.toLowerCase()) {
            case "small":
                sizePrice = 5000;
                break;
            case "medium":
                sizePrice = 7500;
                break;
            case "large":
                sizePrice = 10000;
                break;
        }
        
        // Calculate total cost for all locations
        int totalCost = 0;
        if (locations != null && !locations.trim().isEmpty()) {
            String[] locationArray = locations.split(",");
            for (String location : locationArray) {
                String trimmedLocation = location.trim();
                if (!trimmedLocation.isEmpty()) {
                    // Get daily rate for this specific location
                    int dailyRate = getLocationDailyRate(trimmedLocation);
                    // Calculate cost for this location: (Daily Rate × Duration) + Size Price
                    int locationCost = (dailyRate * duration) + sizePrice;
                    totalCost += locationCost;
                }
            }
        }
        
        return totalCost;
    }
    
    // Get daily rate for specific location
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
    
    // Digital Banner Pricing
    public int calculateDigitalBanner(String websiteTypes, String bannerSize, boolean tracking, int duration) {
        int sizePrice = 0;
        int trackingPrice = tracking ? 1000 : 0;
        int dailyPrice = 500; // Per day
        
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
        
        // Calculate cost per website
        int costPerWebsite = sizePrice + trackingPrice + (dailyPrice * duration);
        
        // Count number of selected website types
        int websiteCount = 1; // Default to 1 if no selection
        System.out.println("=== PRICING SERVICE DEBUG ===");
        System.out.println("Raw Website Types: '" + websiteTypes + "'");
        if (websiteTypes != null && !websiteTypes.trim().isEmpty()) {
            String[] websiteArray = websiteTypes.split(",");
            websiteCount = 0;
            for (String website : websiteArray) {
                if (!website.trim().isEmpty()) {
                    websiteCount++;
                }
            }
        }
        System.out.println("Pricing Service Website Count: " + websiteCount);
        System.out.println("Cost Per Website: " + costPerWebsite);
        
        // Total cost = cost per website × number of websites
        int totalCost = costPerWebsite * websiteCount;
        System.out.println("Total Cost: " + totalCost);
        return totalCost;
    }
    
    // Get pricing breakdown for display
    public Map<String, Object> getPricingBreakdown(String serviceType, Map<String, Object> parameters) {
        Map<String, Object> breakdown = new HashMap<>();
        
        switch (serviceType.toLowerCase()) {
            case "tv":
            case "tv_advertisement":
                String timeSlot = (String) parameters.get("timeSlot");
                int duration = parseInteger((String) parameters.get("duration"), 30);
                int days = parseInteger((String) parameters.get("days"), 1);
                
                int basePrice = 150;
                int timeSlotPrice = 0;
                
                // Calculate time slot price for multiple slots
                if (timeSlot != null && timeSlot.contains(",")) {
                    String[] slots = timeSlot.split(",");
                    for (String slot : slots) {
                        String trimmedSlot = slot.trim().toLowerCase();
                        switch (trimmedSlot) {
                            case "morning":
                                timeSlotPrice += 1000;
                                break;
                            case "evening":
                                timeSlotPrice += 2000;
                                break;
                            case "prime time":
                                timeSlotPrice += 3000;
                                break;
                        }
                    }
                } else {
                    // Single time slot
                    switch (timeSlot.toLowerCase()) {
                        case "morning":
                            timeSlotPrice = 1000;
                            break;
                        case "evening":
                            timeSlotPrice = 2000;
                            break;
                        case "prime time":
                            timeSlotPrice = 3000;
                            break;
                    }
                }
                
                int durationPrice = basePrice * duration;
                int totalPrice = calculateTVAdvertisement(timeSlot, duration, days);
                
                breakdown.put("Base Price (Rs. 150 per second)", "Rs. " + basePrice);
                breakdown.put("Time Slot (" + timeSlot + ")", "Rs. " + timeSlotPrice);
                breakdown.put("Duration (" + duration + " seconds)", "Rs. " + durationPrice);
                breakdown.put("Days (" + days + " day" + (days > 1 ? "s" : "") + ")", "Rs. " + (timeSlotPrice * days));
                breakdown.put("total", totalPrice);
                break;
                
            case "radio":
            case "radio_promotion":
                String radioTimeSlot = (String) parameters.get("timeSlot");
                int radioDuration = parseInteger((String) parameters.get("duration"), 15);
                int plays = parseInteger((String) parameters.get("plays"), 10);
                
                int radioBasePrice = 100;
                int radioTimeSlotPrice = getTimeSlotPrice("radio", radioTimeSlot);
                int radioDurationPrice = radioBasePrice * radioDuration;
                int radioTotalPrice = calculateRadioPromotion(radioTimeSlot, radioDuration, plays);
                
                breakdown.put("Base Price (Rs. 100 per second)", "Rs. " + radioBasePrice);
                breakdown.put("Time Slot (" + radioTimeSlot + ")", "Rs. " + radioTimeSlotPrice);
                breakdown.put("Duration (" + radioDuration + " seconds)", "Rs. " + radioDurationPrice);
                breakdown.put("Plays (" + plays + " play" + (plays > 1 ? "s" : "") + ")", "Rs. " + (radioTimeSlotPrice * plays));
                breakdown.put("total", radioTotalPrice);
                break;
                
            case "social":
            case "social_media_campaign":
                String[] platforms = (String[]) parameters.get("platforms");
                String[] contentTypes = (String[]) parameters.get("contentTypes");
                int socialDuration = parseInteger((String) parameters.get("duration"), 7);
                
                int platformPrice = 2500;
                int dailyPrice = 500;
                
                // Calculate content type costs
                int totalContentCost = 0;
                for (String contentType : contentTypes) {
                    totalContentCost += getContentPrice(contentType);
                }
                
                int socialTotalPrice = calculateSocialMediaCampaign(platforms, contentTypes, socialDuration);
                
                breakdown.put("Platform Setup (per platform)", "Rs. " + platformPrice);
                breakdown.put("Platforms (" + String.join(", ", platforms) + ")", "Rs. " + (platformPrice * platforms.length));
                breakdown.put("Content Types (" + String.join(", ", contentTypes) + ")", "Rs. " + totalContentCost);
                breakdown.put("Duration (" + socialDuration + " day" + (socialDuration > 1 ? "s" : "") + ")", "Rs. " + (dailyPrice * socialDuration));
                breakdown.put("total", socialTotalPrice);
                break;
                
            case "newspaper":
            case "newspaper_ad":
                String newspapers = (String) parameters.get("newspapers");
                String adSize = (String) parameters.get("adSize");
                String colorType = (String) parameters.get("colorType");
                int newspaperDays = parseInteger((String) parameters.get("days"), 1);
                
                int sizePrice = getSizePrice("newspaper", adSize);
                int colorPrice = getColorPrice(colorType);
                int newspaperTotalPrice = calculateNewspaperAd(newspapers, adSize, colorType, newspaperDays);
                
                breakdown.put("Newspapers (" + newspapers + ")", "Rs. " + (newspapers != null && newspapers.contains(",") ? newspapers.split(",").length : 1));
                breakdown.put("Ad Size (" + adSize + ")", "Rs. " + sizePrice);
                breakdown.put("Color Type (" + colorType + ")", "Rs. " + colorPrice);
                breakdown.put("Days (" + newspaperDays + " day" + (newspaperDays > 1 ? "s" : "") + ")", "Rs. " + ((sizePrice + colorPrice) * newspaperDays));
                breakdown.put("total", newspaperTotalPrice);
                break;
                
            case "billboard":
            case "billboard_advertising":
                String billboardLocations = (String) parameters.get("locations");
                String billboardSize = (String) parameters.get("billboardSize");
                int billboardDuration = parseInteger((String) parameters.get("duration"), 7);
                
                int billboardSizePrice = getSizePrice("billboard", billboardSize);
                int billboarddailyPrice = 1000;
                int billboardTotalPrice = calculateBillboardAdvertising(billboardLocations, billboardSize, billboardDuration);
                
                breakdown.put("Locations (" + billboardLocations + ")", "Rs. " + (billboardLocations != null && billboardLocations.contains(",") ? billboardLocations.split(",").length : 1));
                breakdown.put("Billboard Size (" + billboardSize + ")", "Rs. " + billboardSizePrice);
                breakdown.put("Daily Rate (Rs. 1000 per day)", "Rs. " + billboarddailyPrice);
                breakdown.put("Duration (" + billboardDuration + " day" + (billboardDuration > 1 ? "s" : "") + ")", "Rs. " + (billboarddailyPrice * billboardDuration));
                breakdown.put("total", billboardTotalPrice);
                break;
                
            case "digital":
            case "digital_banner":
                String bannerSize = (String) parameters.get("bannerSize");
                String trackingStr = (String) parameters.get("tracking");
                boolean tracking = trackingStr != null && trackingStr.contains("true");
                int bannerDuration = parseInteger((String) parameters.get("duration"), 7);
                
                int bannerSizePrice = getSizePrice("digital", bannerSize);
                int trackingPrice = tracking ? 1000 : 0;
                int bannerDailyPrice = 500;
                String bannerWebsiteTypes = (String) parameters.get("websiteTypes");
                int bannerTotalPrice = calculateDigitalBanner(bannerWebsiteTypes, bannerSize, tracking, bannerDuration);
                
                breakdown.put("Banner Size (" + bannerSize + ")", "Rs. " + bannerSizePrice);
                if (tracking) {
                    breakdown.put("Tracking Feature", "Rs. " + trackingPrice);
                }
                breakdown.put("Daily Rate (Rs. 500 per day)", "Rs. " + bannerDailyPrice);
                breakdown.put("Duration (" + bannerDuration + " day" + (bannerDuration > 1 ? "s" : "") + ")", "Rs. " + (bannerDailyPrice * bannerDuration));
                breakdown.put("total", bannerTotalPrice);
                break;
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
    
    private int getTimeSlotPrice(String service, String timeSlot) {
        if (service.equals("tv")) {
            int totalPrice = 0;
            
            // Handle multiple time slots (comma-separated)
            if (timeSlot != null && timeSlot.contains(",")) {
                String[] slots = timeSlot.split(",");
                for (String slot : slots) {
                    String trimmedSlot = slot.trim().toLowerCase();
                    switch (trimmedSlot) {
                        case "morning":
                            totalPrice += 1000;
                            break;
                        case "evening":
                            totalPrice += 2000;
                            break;
                        case "prime time":
                            totalPrice += 3000;
                            break;
                    }
                }
            } else {
                // Single time slot
                switch (timeSlot.toLowerCase()) {
                    case "morning":
                        totalPrice = 1000;
                        break;
                    case "evening":
                        totalPrice = 2000;
                        break;
                    case "prime time":
                        totalPrice = 3000;
                        break;
                }
            }
            return totalPrice;
        } else if (service.equals("radio")) {
            switch (timeSlot.toLowerCase()) {
                case "morning": return 500;
                case "evening": return 800;
                case "prime time": return 1200;
            }
        }
        return 0;
    }
    
    private int getContentPrice(String contentType) {
        switch (contentType.toLowerCase()) {
            case "image": return 1000;
            case "video": return 2000;
            case "story": return 1500;
        }
        return 0;
    }
    
    private int getSizePrice(String service, String size) {
        if (service.equals("newspaper")) {
            switch (size.toLowerCase()) {
                case "box": return 2000;
                case "half page": return 4000;
                case "full page": return 6000;
            }
        } else if (service.equals("billboard")) {
            switch (size.toLowerCase()) {
                case "small": return 3000;
                case "medium": return 5000;
                case "large": return 8000;
            }
        } else if (service.equals("digital")) {
            switch (size.toLowerCase()) {
                case "small": return 1500;
                case "medium": return 2500;
                case "large": return 4000;
            }
        }
        return 0;
    }
    
    private int getColorPrice(String colorType) {
        switch (colorType.toLowerCase()) {
            case "colour": return 1000;
            case "black & white": return 500;
        }
        return 0;
    }

    public int calculateSocialMedia(String platforms, int socialDuration, int socialDays) {
        // Simple social media pricing calculation
        int platformPrice = 1000; // Per platform
        int dailyPrice = 500; // Per day
        
        // Count platforms (comma-separated)
        String[] platformArray = platforms.split(",");
        int platformCount = platformArray.length;
        
        int totalPlatforms = platformPrice * platformCount;
        int totalDuration = dailyPrice * socialDuration;
        
        return (totalPlatforms + totalDuration) * socialDays;
    }
}
